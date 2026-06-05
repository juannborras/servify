package com.servify;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(classes = ServifyApplication.class)
@AutoConfigureMockMvc
class MvpHttpFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void flujoPrincipalMvp_exponeIdsNecesariosParaReact() throws Exception {
        UUID solicitanteId = crearUsuario("cliente-http@servify.test");
        UUID prestadorId = crearUsuario("prestador-http@servify.test");

        mockMvc.perform(putJson("/api/v1/usuarios/" + prestadorId + "/perfil", """
                {
                  "nombre": "Ana",
                  "apellido": "Gomez",
                  "edad": 32,
                  "fotoPerfilUrl": "https://cdn.servify.test/ana.jpg",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "Palermo",
                    "calle": "Santa Fe",
                    "altura": "1234",
                    "referencia": "PB",
                    "latitud": -34.58,
                    "longitud": -58.42
                  },
                  "descripcionPersonal": "Plomeria general"
                }
                """))
                .andExpect(status().isOk());

        UUID categoriaId = idFrom(postCreatedPath("/api/v1/categorias", """
                {
                  "nombre": "Plomeria HTTP",
                  "descripcion": "Arreglos domesticos"
                }
                """));

        mockMvc.perform(patchJson("/api/v1/categorias/" + categoriaId + "/estado", """
                {
                  "estadoDestino": "ACTIVA",
                  "motivo": "Preparacion MVP"
                }
                """))
                .andExpect(status().isOk());

        UUID publicacionId = idFrom(postCreatedPath("/api/v1/publicaciones", """
                {
                  "usuarioId": "%s",
                  "categoriaServicioId": "%s",
                  "titulo": "Plomera a domicilio HTTP",
                  "descripcion": "Reparaciones y mantenimiento",
                  "modalidadServicio": "PRESENCIAL",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "Palermo",
                    "calle": "Santa Fe",
                    "altura": "1234",
                    "referencia": "PB",
                    "latitud": -34.58,
                    "longitud": -58.42
                  },
                  "disponibilidadesHorarias": [
                    {
                      "diaSemana": "MONDAY",
                      "horaDesde": "09:00:00",
                      "horaHasta": "12:00:00"
                    }
                  ],
                  "precioBase": 1000
                }
                """.formatted(prestadorId, categoriaId)));

        mockMvc.perform(patchJson("/api/v1/publicaciones/" + publicacionId + "/estado", """
                {
                  "usuarioId": "%s",
                  "estadoDestino": "ACTIVA",
                  "motivo": "Lista para recibir solicitudes"
                }
                """.formatted(prestadorId)))
                .andExpect(status().isOk());

        JsonNode publicacionesCategoria = responseJson(getPath("/api/v1/categorias/" + categoriaId + "/publicaciones"));
        assertTrue(publicacionesCategoria.isArray());
        assertEquals(publicacionId.toString(), publicacionesCategoria.get(0).get("id").asText());

        JsonNode reputacionInicial = responseJson(getPath("/api/v1/usuarios/" + prestadorId + "/reputacion"));
        assertEquals(prestadorId.toString(), reputacionInicial.get("usuarioId").asText());
        assertEquals(0, reputacionInicial.get("cantidadValoraciones").asInt());
        assertEquals(0.0, reputacionInicial.get("promedioEstrellas").asDouble());

        UUID solicitudId = idFrom(postCreatedPath("/api/v1/solicitudes", """
                {
                  "solicitanteId": "%s",
                  "categoriaServicioId": "%s",
                  "modalidadServicio": "PRESENCIAL",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "Palermo",
                    "calle": "Santa Fe",
                    "altura": "1400",
                    "referencia": "Depto 2",
                    "latitud": -34.58,
                    "longitud": -58.42
                  },
                  "disponibilidadRequerida": {
                    "diaSemana": "MONDAY",
                    "horaDesde": "10:00:00",
                    "horaHasta": "11:00:00"
                  },
                  "descripcionNecesidad": "Necesito reparar una perdida",
                  "precioReferencia": 1500
                }
                """.formatted(solicitanteId, categoriaId)));

        JsonNode recibidas = responseJson(getPath("/api/v1/prestadores/" + prestadorId + "/solicitudes-recibidas"));
        UUID distribucionId = UUID.fromString(recibidas.get(0).get("distribucionSolicitudId").asText());

        mockMvc.perform(postJson("/api/v1/distribuciones/" + distribucionId + "/respuestas", """
                {
                  "prestadorId": "%s",
                  "tipoRespuesta": "ACEPTAR"
                }
                """.formatted(prestadorId)))
                .andExpect(status().isNoContent());

        JsonNode estadoAceptado = responseJson(getPath("/api/v1/solicitudes/" + solicitudId + "/estado-asignacion"));
        assertEquals("BUSCANDO_PRESTADOR", estadoAceptado.get("estadoSolicitud").asText());
        assertEquals(1, estadoAceptado.get("distribucionesAceptadas").size());
        assertEquals(distribucionId.toString(), estadoAceptado.get("distribucionesAceptadas").get(0).get("id").asText());

        JsonNode asignacion = responseJson(postPath("/api/v1/solicitudes/" + solicitudId + "/asignaciones/confirmaciones", """
                {
                  "distribucionSolicitudId": "%s",
                  "solicitanteId": "%s"
                }
                """.formatted(distribucionId, solicitanteId)));

        UUID asignacionId = UUID.fromString(asignacion.get("id").asText());
        JsonNode estadoAsignado = responseJson(getPath("/api/v1/solicitudes/" + solicitudId + "/estado-asignacion"));
        assertEquals("ASIGNADA", estadoAsignado.get("estadoSolicitud").asText());

        mockMvc.perform(postJson("/api/v1/solicitudes/" + solicitudId + "/finalizaciones/confirmaciones", """
                {
                  "asignacionServicioId": "%s",
                  "confirmanteId": "%s",
                  "rolConfirmante": "SOLICITANTE",
                  "observacion": "Finalizado por solicitante"
                }
                """.formatted(asignacionId, solicitanteId)))
                .andExpect(status().isNoContent());

        JsonNode estadoUnaConfirmacion = responseJson(getPath("/api/v1/solicitudes/" + solicitudId + "/estado-asignacion"));
        assertTrue(estadoUnaConfirmacion.get("confirmadoPorSolicitante").asBoolean());
        assertTrue(!estadoUnaConfirmacion.get("confirmadoPorPrestador").asBoolean());

        mockMvc.perform(postJson("/api/v1/solicitudes/" + solicitudId + "/finalizaciones/confirmaciones", """
                {
                  "asignacionServicioId": "%s",
                  "confirmanteId": "%s",
                  "rolConfirmante": "PRESTADOR",
                  "observacion": "Finalizado por prestador"
                }
                """.formatted(asignacionId, prestadorId)))
                .andExpect(status().isNoContent());

        JsonNode estadoFinalizado = responseJson(getPath("/api/v1/solicitudes/" + solicitudId + "/estado-asignacion"));
        assertTrue(estadoFinalizado.get("finalizacionConfirmada").asBoolean());
        assertEquals("FINALIZADA", estadoFinalizado.get("estadoSolicitud").asText());

        mockMvc.perform(postJson("/api/v1/solicitudes/" + solicitudId + "/calificaciones", """
                {
                  "asignacionServicioId": "%s",
                  "solicitanteId": "%s",
                  "prestadorId": "%s",
                  "calificadorId": "%s",
                  "rolCalificador": "SOLICITANTE",
                  "puntaje": 5
                }
                """.formatted(asignacionId, solicitanteId, prestadorId, solicitanteId)))
                .andExpect(status().isCreated());

        JsonNode calificacionSolicitante = responseJson(getPath(
                "/api/v1/solicitudes/" + solicitudId
                        + "/calificaciones?asignacionServicioId=" + asignacionId
                        + "&rolCalificador=SOLICITANTE"
        ));
        assertEquals(5, calificacionSolicitante.get("puntaje").asInt());
        assertEquals("SOLICITANTE", calificacionSolicitante.get("rolCalificador").asText());

        mockMvc.perform(postJson("/api/v1/solicitudes/" + solicitudId + "/calificaciones", """
                {
                  "asignacionServicioId": "%s",
                  "solicitanteId": "%s",
                  "prestadorId": "%s",
                  "calificadorId": "%s",
                  "rolCalificador": "PRESTADOR",
                  "puntaje": 4
                }
                """.formatted(asignacionId, solicitanteId, prestadorId, prestadorId)))
                .andExpect(status().isCreated());

        JsonNode reputacionPrestador = responseJson(getPath("/api/v1/usuarios/" + prestadorId + "/reputacion"));
        assertEquals(1, reputacionPrestador.get("cantidadValoraciones").asInt());
        assertEquals(5.0, reputacionPrestador.get("promedioEstrellas").asDouble());

        JsonNode reputacionSolicitante = responseJson(getPath("/api/v1/usuarios/" + solicitanteId + "/reputacion"));
        assertEquals(1, reputacionSolicitante.get("cantidadValoraciones").asInt());
        assertEquals(4.0, reputacionSolicitante.get("promedioEstrellas").asDouble());

        assertNotNull(asignacionId);
    }

    @Test
    void listarUsuariosActivos_exponeEndpointSimpleParaVerificarBackendLocal() throws Exception {
        UUID usuarioId = crearUsuario("diagnostico-http@servify.test");

        JsonNode usuarios = responseJson(getPath("/api/v1/usuarios?estado=ACTIVO"));

        assertNotNull(usuarioId);
        org.junit.jupiter.api.Assertions.assertTrue(usuarios.isArray());
        org.junit.jupiter.api.Assertions.assertTrue(usuarios.size() >= 1);
    }

    @Test
    void prestadorConPerfilCompletoSinFotoPuedeCrearPublicacion() throws Exception {
        UUID prestadorId = crearUsuario("prestador-sin-foto-http@servify.test");

        JsonNode perfil = responseJson(putPath("/api/v1/usuarios/" + prestadorId + "/perfil", """
                {
                  "nombre": "Lucia",
                  "apellido": "Paredes",
                  "edad": 29,
                  "fotoPerfilUrl": "",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "Caballito",
                    "calle": "Rivadavia",
                    "altura": "5000",
                    "referencia": "Local",
                    "latitud": -34.61,
                    "longitud": -58.44
                  },
                  "descripcionPersonal": "Electricidad domiciliaria"
                }
                """));
        assertTrue(perfil.get("perfilCompleto").asBoolean());

        UUID categoriaId = crearCategoriaActiva("Electricidad sin foto HTTP");

        UUID publicacionId = idFrom(postCreatedPath("/api/v1/publicaciones", """
                {
                  "usuarioId": "%s",
                  "categoriaServicioId": "%s",
                  "titulo": "Electricista sin foto HTTP",
                  "descripcion": "Instalaciones y arreglos generales",
                  "modalidadServicio": "PRESENCIAL",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "Caballito",
                    "calle": "Rivadavia",
                    "altura": "5000",
                    "referencia": "Local",
                    "latitud": -34.61,
                    "longitud": -58.44
                  },
                  "disponibilidadesHorarias": [
                    {
                      "diaSemana": "TUESDAY",
                      "horaDesde": "10:00:00",
                      "horaHasta": "13:00:00"
                    }
                  ],
                  "precioBase": 2000
                }
                """.formatted(prestadorId, categoriaId)));

        assertNotNull(publicacionId);
    }

    @Test
    void autenticacionSocialGoogle_creaSesionYPermiteRenovarRefreshToken() throws Exception {
        JsonNode sesion = responseJson(postPath("/api/v1/auth/social/google", """
                {
                  "idToken": "fake:google:google-sub-http:social-http@servify.test",
                  "rol": "USUARIO"
                }
                """));

        assertNotNull(sesion.get("usuarioId").asText());
        assertEquals("social-http@servify.test", sesion.get("emailAcceso").asText());

        String refreshToken = sesion.get("refreshToken").get("token").asText();
        JsonNode sesionRenovada = responseJson(postPath("/api/v1/auth/refresh", """
                {
                  "refreshToken": "%s"
                }
                """.formatted(refreshToken)));

        assertEquals(sesion.get("usuarioId").asText(), sesionRenovada.get("usuarioId").asText());
        assertEquals("social-http@servify.test", sesionRenovada.get("emailAcceso").asText());

        UUID usuarioSocialId = UUID.fromString(sesion.get("usuarioId").asText());
        JsonNode perfilSocial = responseJson(getPath("/api/v1/usuarios/" + usuarioSocialId + "/perfil"));
        assertEquals("Usuario", perfilSocial.get("nombre").asText());
        assertEquals("google", perfilSocial.get("apellido").asText());

        UUID categoriaId = crearCategoriaActiva("Servicios social Google HTTP");
        UUID publicacionId = idFrom(postCreatedPath("/api/v1/publicaciones", """
                {
                  "usuarioId": "%s",
                  "categoriaServicioId": "%s",
                  "titulo": "Servicio publicado con Google HTTP",
                  "descripcion": "Publicacion creada por usuario social",
                  "modalidadServicio": "PRESENCIAL",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "Palermo",
                    "calle": "Santa Fe",
                    "altura": "1234",
                    "referencia": "PB",
                    "latitud": -34.58,
                    "longitud": -58.42
                  },
                  "disponibilidadesHorarias": [
                    {
                      "diaSemana": "WEDNESDAY",
                      "horaDesde": "09:00:00",
                      "horaHasta": "12:00:00"
                    }
                  ],
                  "precioBase": 1800
                }
                """.formatted(usuarioSocialId, categoriaId)));

        assertNotNull(publicacionId);
    }

    @Test
    void autenticacionSocialLinkedin_vinculaUsuarioExistentePorEmailVerificado() throws Exception {
        UUID usuarioId = crearUsuario("linkedin-http@servify.test");

        JsonNode sesion = responseJson(postPath("/api/v1/auth/social/linkedin", """
                {
                  "idToken": "fake:linkedin:linkedin-sub-http:linkedin-http@servify.test"
                }
                """));

        assertEquals(usuarioId.toString(), sesion.get("usuarioId").asText());
        assertEquals("linkedin-http@servify.test", sesion.get("emailAcceso").asText());
    }

    @Test
    void solicitudSinPrecioReferenciaNoBloqueaPublicacionCompatibleConPrecioBase() throws Exception {
        UUID solicitanteId = crearUsuario("cliente-sin-precio-http@servify.test");
        UUID prestadorId = crearUsuario("guitarra-sin-precio-http@servify.test");
        completarPerfilPalermo(prestadorId);
        UUID categoriaId = crearCategoriaActiva("Clases particulares sin precio HTTP");

        UUID publicacionId = crearPublicacionActiva(prestadorId, categoriaId, "Clases de guitarra sin precio HTTP", "Palermo", 15000);
        assertNotNull(publicacionId);

        UUID solicitudId = idFrom(postCreatedPath("/api/v1/solicitudes", """
                {
                  "solicitanteId": "%s",
                  "categoriaServicioId": "%s",
                  "modalidadServicio": "PRESENCIAL",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "Palermo",
                    "calle": "Santa Fe",
                    "altura": "1400",
                    "referencia": "Depto 2",
                    "latitud": -34.5889,
                    "longitud": -58.4306
                  },
                  "disponibilidadRequerida": {
                    "diaSemana": "MONDAY",
                    "horaDesde": "10:00:00",
                    "horaHasta": "11:00:00"
                  },
                  "descripcionNecesidad": "Busco una clase inicial de guitarra"
                }
                """.formatted(solicitanteId, categoriaId)));

        JsonNode recibidas = responseJson(getPath("/api/v1/prestadores/" + prestadorId + "/solicitudes-recibidas"));
        assertNotNull(solicitudId);
        assertEquals(1, recibidas.size());
        assertEquals(publicacionId.toString(), recibidas.get(0).get("publicacionServicioId").asText());
    }

    @Test
    void precioReferenciaMenorNoBloqueaMatchingPorquePrestadorPuedeContraofertar() throws Exception {
        UUID solicitanteId = crearUsuario("cliente-precio-menor-http@servify.test");
        UUID prestadorId = crearUsuario("guitarra-precio-menor-http@servify.test");
        completarPerfilPalermo(prestadorId);
        UUID categoriaId = crearCategoriaActiva("Clases particulares precio menor HTTP");

        UUID publicacionId = crearPublicacionActiva(prestadorId, categoriaId, "Clases de guitarra precio menor HTTP", "Palermo", 15000);

        UUID solicitudId = idFrom(postCreatedPath("/api/v1/solicitudes", """
                {
                  "solicitanteId": "%s",
                  "categoriaServicioId": "%s",
                  "modalidadServicio": "PRESENCIAL",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "Palermo",
                    "calle": "Santa Fe",
                    "altura": "1400",
                    "referencia": "Depto 2",
                    "latitud": -34.5889,
                    "longitud": -58.4306
                  },
                  "disponibilidadRequerida": {
                    "diaSemana": "MONDAY",
                    "horaDesde": "10:00:00",
                    "horaHasta": "11:00:00"
                  },
                  "descripcionNecesidad": "Busco una clase inicial de guitarra con presupuesto flexible",
                  "precioReferencia": 10000
                }
                """.formatted(solicitanteId, categoriaId)));

        JsonNode recibidas = responseJson(getPath("/api/v1/prestadores/" + prestadorId + "/solicitudes-recibidas"));

        assertNotNull(solicitudId);
        assertEquals(1, recibidas.size());
        assertEquals(publicacionId.toString(), recibidas.get(0).get("publicacionServicioId").asText());
    }

    @Test
    void disponibilidadSolapadaGeneraMatchingAunqueNoSeaIdentica() throws Exception {
        UUID solicitanteId = crearUsuario("cliente-gasista-solapado-http@servify.test");
        UUID prestadorId = crearUsuario("gasista-solapado-http@servify.test");
        completarPerfilPalermo(prestadorId);
        UUID categoriaId = crearCategoriaActiva("Gasista solapado HTTP");

        UUID publicacionId = idFrom(postCreatedPath("/api/v1/publicaciones", """
                {
                  "usuarioId": "%s",
                  "categoriaServicioId": "%s",
                  "titulo": "Gasista matriculado solapado HTTP",
                  "descripcion": "Reparaciones de gas y revisiones",
                  "modalidadServicio": "PRESENCIAL",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "Palermo",
                    "calle": "Santa Fe",
                    "altura": "1234",
                    "referencia": "PB",
                    "latitud": -34.5889,
                    "longitud": -58.4306
                  },
                  "disponibilidadesHorarias": [
                    {
                      "diaSemana": "MONDAY",
                      "horaDesde": "10:00:00",
                      "horaHasta": "12:00:00"
                    }
                  ],
                  "precioBase": 18000
                }
                """.formatted(prestadorId, categoriaId)));

        putOrPatchPublicacionEstado(publicacionId, prestadorId, "ACTIVA");

        UUID solicitudId = idFrom(postCreatedPath("/api/v1/solicitudes", """
                {
                  "solicitanteId": "%s",
                  "categoriaServicioId": "%s",
                  "modalidadServicio": "PRESENCIAL",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "Palermo",
                    "calle": "Santa Fe",
                    "altura": "1400",
                    "referencia": "Depto 2",
                    "latitud": -34.5889,
                    "longitud": -58.4306
                  },
                  "disponibilidadRequerida": {
                    "diaSemana": "MONDAY",
                    "horaDesde": "11:30:00",
                    "horaHasta": "13:00:00"
                  },
                  "descripcionNecesidad": "Necesito reparar una perdida de gas",
                  "precioReferencia": 20000
                }
                """.formatted(solicitanteId, categoriaId)));

        JsonNode recibidas = responseJson(getPath("/api/v1/prestadores/" + prestadorId + "/solicitudes-recibidas"));
        assertNotNull(solicitudId);
        assertEquals(1, recibidas.size());
        assertEquals(publicacionId.toString(), recibidas.get(0).get("publicacionServicioId").asText());
    }

    @Test
    void rangoDeDiasDePublicacionMatcheaSolicitudEnDiaIntermedio() throws Exception {
        UUID solicitanteId = crearUsuario("cliente-gasista-rango-http@servify.test");
        UUID prestadorId = crearUsuario("gasista-rango-http@servify.test");
        completarPerfilPalermo(prestadorId);
        UUID categoriaId = crearCategoriaActiva("Gasista rango dias HTTP");

        UUID publicacionId = idFrom(postCreatedPath("/api/v1/publicaciones", """
                {
                  "usuarioId": "%s",
                  "categoriaServicioId": "%s",
                  "titulo": "Gasista con rango semanal HTTP",
                  "descripcion": "Reparaciones de gas de lunes a viernes",
                  "modalidadServicio": "PRESENCIAL",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "Palermo",
                    "calle": "Santa Fe",
                    "altura": "1234",
                    "referencia": "PB",
                    "latitud": -34.5889,
                    "longitud": -58.4306
                  },
                  "disponibilidadesHorarias": [
                    { "diaSemana": "MONDAY", "horaDesde": "09:00:00", "horaHasta": "18:00:00" },
                    { "diaSemana": "TUESDAY", "horaDesde": "09:00:00", "horaHasta": "18:00:00" },
                    { "diaSemana": "WEDNESDAY", "horaDesde": "09:00:00", "horaHasta": "18:00:00" },
                    { "diaSemana": "THURSDAY", "horaDesde": "09:00:00", "horaHasta": "18:00:00" },
                    { "diaSemana": "FRIDAY", "horaDesde": "09:00:00", "horaHasta": "18:00:00" }
                  ],
                  "precioBase": 18000
                }
                """.formatted(prestadorId, categoriaId)));

        putOrPatchPublicacionEstado(publicacionId, prestadorId, "ACTIVA");

        UUID solicitudId = idFrom(postCreatedPath("/api/v1/solicitudes", """
                {
                  "solicitanteId": "%s",
                  "categoriaServicioId": "%s",
                  "modalidadServicio": "PRESENCIAL",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "Palermo",
                    "calle": "Santa Fe",
                    "altura": "1400",
                    "referencia": "Depto 2",
                    "latitud": -34.5889,
                    "longitud": -58.4306
                  },
                  "disponibilidadRequerida": {
                    "diaSemana": "WEDNESDAY",
                    "horaDesde": "11:00:00",
                    "horaHasta": "12:00:00"
                  },
                  "descripcionNecesidad": "Necesito reparar un calefon",
                  "precioReferencia": 20000
                }
                """.formatted(solicitanteId, categoriaId)));

        JsonNode recibidas = responseJson(getPath("/api/v1/prestadores/" + prestadorId + "/solicitudes-recibidas"));
        assertNotNull(solicitudId);
        assertEquals(1, recibidas.size());
        assertEquals(publicacionId.toString(), recibidas.get(0).get("publicacionServicioId").asText());
    }

    @Test
    void matchingPriorizaServicioRelevanteAunqueOtraPublicacionSeaMasReciente() throws Exception {
        UUID solicitanteId = crearUsuario("cliente-relevancia-servicio-http@servify.test");
        UUID prestadorId = crearUsuario("prestador-relevancia-servicio-http@servify.test");
        completarPerfilPalermo(prestadorId);
        UUID categoriaId = crearCategoriaActiva("Reparaciones relevancia HTTP");

        UUID gasistaId = crearPublicacionActiva(
                prestadorId,
                categoriaId,
                "Gasista matriculado HTTP",
                "Palermo",
                20000
        );
        UUID plomeroId = crearPublicacionActiva(
                prestadorId,
                categoriaId,
                "Plomero sanitario HTTP",
                "Palermo",
                18000
        );

        UUID solicitudId = idFrom(postCreatedPath("/api/v1/solicitudes", """
                {
                  "solicitanteId": "%s",
                  "categoriaServicioId": "%s",
                  "modalidadServicio": "PRESENCIAL",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "Palermo",
                    "calle": "Santa Fe",
                    "altura": "1400",
                    "referencia": "Depto 2",
                    "latitud": -34.5889,
                    "longitud": -58.4306
                  },
                  "disponibilidadRequerida": {
                    "diaSemana": "MONDAY",
                    "horaDesde": "10:00:00",
                    "horaHasta": "11:00:00"
                  },
                  "descripcionNecesidad": "Necesito reparar un calefon de gas",
                  "precioReferencia": 22000
                }
                """.formatted(solicitanteId, categoriaId)));

        JsonNode recibidas = responseJson(getPath("/api/v1/prestadores/" + prestadorId + "/solicitudes-recibidas"));
        assertNotNull(solicitudId);
        assertNotNull(plomeroId);
        assertEquals(1, recibidas.size());
        assertEquals(gasistaId.toString(), recibidas.get(0).get("publicacionServicioId").asText());
    }

    @Test
    void reintentarDistribucionEncuentraPublicacionCreadaDespuesDeLaSolicitud() throws Exception {
        UUID solicitanteId = crearUsuario("cliente-reintento-matching-http@servify.test");
        UUID prestadorId = crearUsuario("prestador-reintento-matching-http@servify.test");
        completarPerfilPalermo(prestadorId);
        UUID categoriaId = crearCategoriaActiva("Reintento matching HTTP");

        UUID solicitudId = idFrom(postCreatedPath("/api/v1/solicitudes", """
                {
                  "solicitanteId": "%s",
                  "categoriaServicioId": "%s",
                  "modalidadServicio": "PRESENCIAL",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "Palermo",
                    "calle": "Santa Fe",
                    "altura": "1400",
                    "referencia": "Depto 2",
                    "latitud": -34.5889,
                    "longitud": -58.4306
                  },
                  "disponibilidadRequerida": {
                    "diaSemana": "THURSDAY",
                    "horaDesde": "12:00:00",
                    "horaHasta": "13:00:00"
                  },
                  "descripcionNecesidad": "Necesito reparar un calefon",
                  "precioReferencia": 20000
                }
                """.formatted(solicitanteId, categoriaId)));

        JsonNode recibidasAntes = responseJson(getPath("/api/v1/prestadores/" + prestadorId + "/solicitudes-recibidas"));
        assertEquals(0, recibidasAntes.size());

        UUID publicacionId = idFrom(postCreatedPath("/api/v1/publicaciones", """
                {
                  "usuarioId": "%s",
                  "categoriaServicioId": "%s",
                  "titulo": "Gasista post solicitud HTTP",
                  "descripcion": "Reparaciones de gas",
                  "modalidadServicio": "PRESENCIAL",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "Palermo",
                    "calle": "Santa Fe",
                    "altura": "1234",
                    "referencia": "PB",
                    "latitud": -34.5889,
                    "longitud": -58.4306
                  },
                  "disponibilidadesHorarias": [
                    { "diaSemana": "THURSDAY", "horaDesde": "09:00:00", "horaHasta": "18:00:00" }
                  ],
                  "precioBase": 18000
                }
                """.formatted(prestadorId, categoriaId)));

        putOrPatchPublicacionEstado(publicacionId, prestadorId, "ACTIVA");

        JsonNode nuevasDistribuciones = responseJson(postPath(
                "/api/v1/solicitudes/" + solicitudId + "/distribuciones/reintentos",
                "{}"
        ));
        assertEquals(1, nuevasDistribuciones.size());
        assertEquals(publicacionId.toString(), nuevasDistribuciones.get(0).get("publicacionServicioId").asText());

        JsonNode recibidasDespues = responseJson(getPath("/api/v1/prestadores/" + prestadorId + "/solicitudes-recibidas"));
        assertEquals(1, recibidasDespues.size());
        assertEquals(publicacionId.toString(), recibidasDespues.get(0).get("publicacionServicioId").asText());
    }

    @Test
    void editarSolicitudActivaRedistribuyeConNuevaDisponibilidadCompatible() throws Exception {
        UUID solicitanteId = crearUsuario("cliente-edita-redistribuye-http@servify.test");
        UUID prestadorId = crearUsuario("prestador-edita-redistribuye-http@servify.test");
        completarPerfilPalermo(prestadorId);
        UUID categoriaId = crearCategoriaActiva("Edita redistribuye HTTP");
        UUID publicacionId = idFrom(postCreatedPath("/api/v1/publicaciones", """
                {
                  "usuarioId": "%s",
                  "categoriaServicioId": "%s",
                  "titulo": "Gasista jueves HTTP",
                  "descripcion": "Reparaciones de gas",
                  "modalidadServicio": "PRESENCIAL",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "Palermo",
                    "calle": "Santa Fe",
                    "altura": "1234",
                    "referencia": "PB",
                    "latitud": -34.5889,
                    "longitud": -58.4306
                  },
                  "disponibilidadesHorarias": [
                    { "diaSemana": "THURSDAY", "horaDesde": "09:00:00", "horaHasta": "18:00:00" }
                  ],
                  "precioBase": 18000
                }
                """.formatted(prestadorId, categoriaId)));
        putOrPatchPublicacionEstado(publicacionId, prestadorId, "ACTIVA");

        UUID solicitudId = idFrom(postCreatedPath("/api/v1/solicitudes", """
                {
                  "solicitanteId": "%s",
                  "categoriaServicioId": "%s",
                  "modalidadServicio": "PRESENCIAL",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "Palermo",
                    "calle": "Santa Fe",
                    "altura": "1400",
                    "referencia": "Depto 2",
                    "latitud": -34.5889,
                    "longitud": -58.4306
                  },
                  "disponibilidadRequerida": {
                    "diaSemana": "MONDAY",
                    "horaDesde": "12:00:00",
                    "horaHasta": "13:00:00"
                  },
                  "descripcionNecesidad": "Necesito reparar un calefon",
                  "precioReferencia": 20000
                }
                """.formatted(solicitanteId, categoriaId)));

        JsonNode recibidasAntes = responseJson(getPath("/api/v1/prestadores/" + prestadorId + "/solicitudes-recibidas"));
        assertEquals(0, recibidasAntes.size());

        responseJson(putPath("/api/v1/solicitudes/" + solicitudId, """
                {
                  "solicitanteId": "%s",
                  "modalidadServicio": "PRESENCIAL",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "Palermo",
                    "calle": "Santa Fe",
                    "altura": "1400",
                    "referencia": "Depto 2",
                    "latitud": -34.5889,
                    "longitud": -58.4306
                  },
                  "disponibilidadRequerida": {
                    "diaSemana": "THURSDAY",
                    "horaDesde": "12:00:00",
                    "horaHasta": "13:00:00"
                  },
                  "descripcionNecesidad": "Necesito reparar un calefon el jueves",
                  "precioReferencia": 20000
                }
                """.formatted(solicitanteId)));

        JsonNode recibidasDespues = responseJson(getPath("/api/v1/prestadores/" + prestadorId + "/solicitudes-recibidas"));
        assertEquals(1, recibidasDespues.size());
        assertEquals(publicacionId.toString(), recibidasDespues.get(0).get("publicacionServicioId").asText());
    }

    @Test
    void solicitantePuedeEditarYCancelarSolicitudActiva() throws Exception {
        UUID solicitanteId = crearUsuario("cliente-edita-solicitud-http@servify.test");
        UUID categoriaId = crearCategoriaActiva("Solicitudes editables HTTP");

        UUID solicitudId = idFrom(postCreatedPath("/api/v1/solicitudes", """
                {
                  "solicitanteId": "%s",
                  "categoriaServicioId": "%s",
                  "modalidadServicio": "PRESENCIAL",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "Palermo",
                    "calle": "Santa Fe",
                    "altura": "1400",
                    "referencia": "Depto 2",
                    "latitud": -34.5889,
                    "longitud": -58.4306
                  },
                  "disponibilidadRequerida": {
                    "diaSemana": "MONDAY",
                    "horaDesde": "10:00:00",
                    "horaHasta": "11:00:00"
                  },
                  "descripcionNecesidad": "Necesito una clase inicial",
                  "precioReferencia": 10000
                }
                """.formatted(solicitanteId, categoriaId)));

        JsonNode actualizada = responseJson(putPath("/api/v1/solicitudes/" + solicitudId, """
                {
                  "solicitanteId": "%s",
                  "modalidadServicio": "VIRTUAL",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "Belgrano",
                    "calle": "Cabildo",
                    "altura": "1900",
                    "referencia": "Online",
                    "latitud": -34.5621,
                    "longitud": -58.4567
                  },
                  "disponibilidadRequerida": {
                    "diaSemana": "TUESDAY",
                    "horaDesde": "14:00:00",
                    "horaHasta": "15:00:00"
                  },
                  "descripcionNecesidad": "Necesito una clase inicial editada",
                  "precioReferencia": 12000
                }
                """.formatted(solicitanteId)));

        assertEquals("VIRTUAL", actualizada.get("modalidadServicio").asText());
        assertEquals("Belgrano", actualizada.get("ubicacion").get("localidad").asText());
        assertEquals("TUESDAY", actualizada.get("disponibilidadRequerida").get("diaSemana").asText());
        assertEquals("Necesito una clase inicial editada", actualizada.get("descripcionNecesidad").asText());

        mockMvc.perform(deleteJson("/api/v1/solicitudes/" + solicitudId, """
                {
                  "solicitanteId": "%s"
                }
                """.formatted(solicitanteId)))
                .andExpect(status().isNoContent());

        JsonNode cancelada = responseJson(getPath("/api/v1/solicitudes/" + solicitudId));
        assertEquals("CANCELADA", cancelada.get("estado").asText());
    }

    @Test
    void contraofertaPendienteSeExponeYPuedeResolversePorHttp() throws Exception {
        UUID solicitanteId = crearUsuario("cliente-contraoferta-http@servify.test");
        UUID prestadorId = crearUsuario("prestador-contraoferta-http@servify.test");
        completarPerfilPalermo(prestadorId);
        UUID categoriaId = crearCategoriaActiva("Contraofertas HTTP");
        UUID publicacionId = crearPublicacionActiva(prestadorId, categoriaId, "Clase con contraoferta HTTP", "Palermo", 15000);

        UUID solicitudId = idFrom(postCreatedPath("/api/v1/solicitudes", """
                {
                  "solicitanteId": "%s",
                  "categoriaServicioId": "%s",
                  "modalidadServicio": "PRESENCIAL",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "Palermo",
                    "calle": "Santa Fe",
                    "altura": "1400",
                    "referencia": "Depto 2",
                    "latitud": -34.5889,
                    "longitud": -58.4306
                  },
                  "disponibilidadRequerida": {
                    "diaSemana": "MONDAY",
                    "horaDesde": "10:00:00",
                    "horaHasta": "11:00:00"
                  },
                  "descripcionNecesidad": "Busco una clase con contraoferta",
                  "precioReferencia": 10000
                }
                """.formatted(solicitanteId, categoriaId)));

        JsonNode recibidas = responseJson(getPath("/api/v1/prestadores/" + prestadorId + "/solicitudes-recibidas"));
        UUID distribucionId = UUID.fromString(recibidas.get(0).get("distribucionSolicitudId").asText());

        mockMvc.perform(postJson("/api/v1/distribuciones/" + distribucionId + "/contraofertas", """
                {
                  "prestadorId": "%s",
                  "precioPropuesto": 13000,
                  "mensaje": "Puedo hacerlo con material incluido"
                }
                """.formatted(prestadorId)))
                .andExpect(status().isAccepted());

        JsonNode estadoContraoferta = responseJson(getPath("/api/v1/solicitudes/" + solicitudId + "/estado-asignacion"));
        assertEquals(1, estadoContraoferta.get("contraofertasPendientes").size());
        UUID contraofertaId = UUID.fromString(estadoContraoferta.get("contraofertasPendientes").get(0).get("id").asText());
        assertEquals(distribucionId.toString(), estadoContraoferta.get("contraofertasPendientes").get(0).get("distribucionSolicitudId").asText());

        JsonNode resuelta = responseJson(postPath("/api/v1/contraofertas/" + contraofertaId + "/resoluciones", """
                {
                  "solicitanteId": "%s",
                  "decision": "ACEPTAR"
                }
                """.formatted(solicitanteId)));
        assertEquals("ACEPTADA", resuelta.get("estado").asText());

        JsonNode estadoAceptado = responseJson(getPath("/api/v1/solicitudes/" + solicitudId + "/estado-asignacion"));
        assertEquals(1, estadoAceptado.get("distribucionesAceptadas").size());
        assertEquals(publicacionId.toString(), estadoAceptado.get("distribucionesAceptadas").get(0).get("publicacionServicioId").asText());
    }

    @Test
    void eliminarPublicacionUsaBorradoLogicoYLaOcultaDeLaCategoria() throws Exception {
        UUID prestadorId = crearUsuario("delete-publicacion-http@servify.test");
        completarPerfilPalermo(prestadorId);
        UUID categoriaId = crearCategoriaActiva("Publicaciones eliminadas HTTP");
        UUID publicacionId = crearPublicacionActiva(prestadorId, categoriaId, "Servicio eliminable HTTP", "Palermo", 9000);

        JsonNode eliminada = responseJson(putOrPatchPublicacionEstado(publicacionId, prestadorId, "ELIMINADA"));
        assertEquals("ELIMINADA", eliminada.get("estado").asText());

        JsonNode publicacionesCategoria = responseJson(getPath("/api/v1/categorias/" + categoriaId + "/publicaciones"));
        assertTrue(publicacionesCategoria.isArray());
        assertEquals(0, publicacionesCategoria.size());
    }

    @Test
    void permiteMismoServicioEnVariasLocalidadesDelMismoPrestador() throws Exception {
        UUID prestadorId = crearUsuario("multi-zona-http@servify.test");
        completarPerfilPalermo(prestadorId);
        UUID categoriaId = crearCategoriaActiva("Multi zona HTTP");

        UUID palermoId = idFrom(postCreatedPath("/api/v1/publicaciones", publicacionJson(
                prestadorId,
                categoriaId,
                "Clases de guitarra multi zona HTTP",
                "Palermo",
                -34.5889,
                -58.4306,
                15000
        )));
        UUID belgranoId = idFrom(postCreatedPath("/api/v1/publicaciones", publicacionJson(
                prestadorId,
                categoriaId,
                "Clases de guitarra multi zona HTTP",
                "Belgrano",
                -34.5621,
                -58.4567,
                15000
        )));

        assertNotNull(palermoId);
        assertNotNull(belgranoId);
    }

    @Test
    void corsPermiteFrontendLocalYLanParaDesarrollo() throws Exception {
        mockMvc.perform(options("/api/v1/usuarios")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));

        mockMvc.perform(options("/api/v1/usuarios")
                        .header("Origin", "http://192.168.1.51:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://192.168.1.51:5173"));

        mockMvc.perform(options("/api/v1/usuarios")
                        .header("Origin", "http://127.0.0.1:8081")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://127.0.0.1:8081"));
    }

    private UUID crearUsuario(String email) throws Exception {
        return idFrom(postCreatedPath("/api/v1/usuarios", """
                {
                  "email": "%s",
                  "telefono": "1111",
                  "rol": "USUARIO"
                }
                """.formatted(email)));
    }

    private void completarPerfilPalermo(UUID usuarioId) throws Exception {
        mockMvc.perform(putJson("/api/v1/usuarios/" + usuarioId + "/perfil", """
                {
                  "nombre": "Juan",
                  "apellido": "Guitarra",
                  "edad": 30,
                  "fotoPerfilUrl": "",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "Palermo",
                    "calle": "Santa Fe",
                    "altura": "1234",
                    "referencia": "PB",
                    "latitud": -34.5889,
                    "longitud": -58.4306
                  },
                  "descripcionPersonal": "Profesor de guitarra"
                }
                """))
                .andExpect(status().isOk());
    }

    private UUID crearPublicacionActiva(
            UUID prestadorId,
            UUID categoriaId,
            String titulo,
            String localidad,
            int precioBase
    ) throws Exception {
        UUID publicacionId = idFrom(postCreatedPath("/api/v1/publicaciones", publicacionJson(
                prestadorId,
                categoriaId,
                titulo,
                localidad,
                localidad.equalsIgnoreCase("Belgrano") ? -34.5621 : -34.5889,
                localidad.equalsIgnoreCase("Belgrano") ? -58.4567 : -58.4306,
                precioBase
        )));

        putOrPatchPublicacionEstado(publicacionId, prestadorId, "ACTIVA");
        return publicacionId;
    }

    private String putOrPatchPublicacionEstado(UUID publicacionId, UUID usuarioId, String estadoDestino) throws Exception {
        return mockMvc.perform(patchJson("/api/v1/publicaciones/" + publicacionId + "/estado", """
                {
                  "usuarioId": "%s",
                  "estadoDestino": "%s",
                  "motivo": "Cambio desde test"
                }
                """.formatted(usuarioId, estadoDestino)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String publicacionJson(
            UUID prestadorId,
            UUID categoriaId,
            String titulo,
            String localidad,
            double latitud,
            double longitud,
            int precioBase
    ) {
        return """
                {
                  "usuarioId": "%s",
                  "categoriaServicioId": "%s",
                  "titulo": "%s",
                  "descripcion": "Servicio de prueba para matching",
                  "modalidadServicio": "PRESENCIAL",
                  "ubicacion": {
                    "pais": "Argentina",
                    "provincia": "Buenos Aires",
                    "ciudad": "CABA",
                    "localidad": "%s",
                    "calle": "Santa Fe",
                    "altura": "1234",
                    "referencia": "PB",
                    "latitud": %s,
                    "longitud": %s
                  },
                  "disponibilidadesHorarias": [
                    {
                      "diaSemana": "MONDAY",
                      "horaDesde": "09:00:00",
                      "horaHasta": "18:00:00"
                    }
                  ],
                  "precioBase": %d
                }
                """.formatted(prestadorId, categoriaId, titulo, localidad, latitud, longitud, precioBase);
    }

    private UUID crearCategoriaActiva(String nombre) throws Exception {
        UUID categoriaId = idFrom(postCreatedPath("/api/v1/categorias", """
                {
                  "nombre": "%s",
                  "descripcion": "Categoria creada por test HTTP"
                }
                """.formatted(nombre)));

        mockMvc.perform(patchJson("/api/v1/categorias/" + categoriaId + "/estado", """
                {
                  "estadoDestino": "ACTIVA",
                  "motivo": "Preparacion test"
                }
                """))
                .andExpect(status().isOk());
        return categoriaId;
    }

    private UUID idFrom(String json) throws Exception {
        return UUID.fromString(objectMapper.readTree(json).get("id").asText());
    }

    private JsonNode responseJson(String json) throws Exception {
        return objectMapper.readTree(json);
    }

    private String getPath(String path) throws Exception {
        return mockMvc.perform(get(path))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String postPath(String path, String json) throws Exception {
        return mockMvc.perform(postJson(path, json))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String putPath(String path, String json) throws Exception {
        return mockMvc.perform(putJson(path, json))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String postCreatedPath(String path, String json) throws Exception {
        return mockMvc.perform(postJson(path, json))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private org.springframework.test.web.servlet.RequestBuilder postJson(String path, String json) {
        return post(path).contentType(MediaType.APPLICATION_JSON).content(json);
    }

    private org.springframework.test.web.servlet.RequestBuilder putJson(String path, String json) {
        return org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);
    }

    private org.springframework.test.web.servlet.RequestBuilder patchJson(String path, String json) {
        return patch(path).contentType(MediaType.APPLICATION_JSON).content(json);
    }

    private org.springframework.test.web.servlet.RequestBuilder deleteJson(String path, String json) {
        return delete(path).contentType(MediaType.APPLICATION_JSON).content(json);
    }
}
