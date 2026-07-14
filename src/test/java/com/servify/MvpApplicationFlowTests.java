package com.servify;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.servify.administracion.application.dto.ModerarPublicacionCommand;
import com.servify.administracion.application.port.out.PublicacionModerablePort;
import com.servify.administracion.application.port.out.UsuarioAdministrablePort;
import com.servify.administracion.application.service.ModerarPublicacionService;
import com.servify.administracion.domain.enumtype.TipoMedida;
import com.servify.autenticacion.application.dto.IniciarSesionCommand;
import com.servify.autenticacion.application.dto.RegistrarCredencialesCommand;
import com.servify.autenticacion.application.dto.TokenResult;
import com.servify.autenticacion.application.port.out.CredencialAccesoRepositoryPort;
import com.servify.autenticacion.application.port.out.PasswordHasherPort;
import com.servify.autenticacion.application.port.out.RefreshTokenRepositoryPort;
import com.servify.autenticacion.application.port.out.TokenProviderPort;
import com.servify.autenticacion.application.port.out.UsuarioAutenticablePort;
import com.servify.autenticacion.application.service.IniciarSesionService;
import com.servify.autenticacion.application.service.RegistrarCredencialesService;
import com.servify.autenticacion.domain.model.CredencialAcceso;
import com.servify.autenticacion.domain.model.RefreshToken;
import com.servify.publicaciones.application.dto.CambiarEstadoCategoriaServicioCommand;
import com.servify.publicaciones.application.dto.CambiarEstadoPublicacionCommand;
import com.servify.publicaciones.application.dto.CategoriaServicioResult;
import com.servify.publicaciones.application.dto.CrearCategoriaServicioCommand;
import com.servify.publicaciones.application.dto.CrearPublicacionCommand;
import com.servify.publicaciones.application.dto.PublicacionServicioResult;
import com.servify.publicaciones.application.port.out.CategoriaServicioRepositoryPort;
import com.servify.publicaciones.application.port.out.PublicacionServicioRepositoryPort;
import com.servify.publicaciones.application.port.out.UsuarioPublicadorPort;
import com.servify.publicaciones.application.service.CambiarEstadoCategoriaServicioService;
import com.servify.publicaciones.application.service.CambiarEstadoPublicacionService;
import com.servify.publicaciones.application.service.CrearCategoriaServicioService;
import com.servify.publicaciones.application.service.CrearPublicacionService;
import com.servify.publicaciones.domain.enumtype.EstadoCategoria;
import com.servify.publicaciones.domain.enumtype.EstadoPublicacion;
import com.servify.publicaciones.domain.model.CategoriaServicio;
import com.servify.publicaciones.domain.model.PublicacionServicio;
import com.servify.publicaciones.domain.service.PoliticaCompatibilidadPublicacion;
import com.servify.publicaciones.domain.service.ValidadorDisponibilidadHoraria;
import com.servify.shared.domain.enumtype.ModalidadServicio;
import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.valueobject.Ubicacion;
import com.servify.solicitudes.application.dto.AsignacionServicioResult;
import com.servify.solicitudes.application.dto.CalificarServicioCommand;
import com.servify.solicitudes.application.dto.ConfirmarAsignacionSolicitudCommand;
import com.servify.solicitudes.application.dto.ConfirmarFinalizacionServicioCommand;
import com.servify.solicitudes.application.dto.ContraofertaResult;
import com.servify.solicitudes.application.dto.CrearSolicitudServicioCommand;
import com.servify.solicitudes.application.dto.EmitirContraofertaCommand;
import com.servify.solicitudes.application.dto.ResolverContraofertaCommand;
import com.servify.solicitudes.application.dto.ResponderDistribucionSolicitudCommand;
import com.servify.solicitudes.application.dto.SolicitudServicioResult;
import com.servify.solicitudes.application.dto.TipoDecisionSolicitud;
import com.servify.solicitudes.application.dto.TipoRespuestaDistribucion;
import com.servify.solicitudes.application.port.out.AsignacionServicioRepositoryPort;
import com.servify.solicitudes.application.port.out.CalificacionRepositoryPort;
import com.servify.solicitudes.application.port.out.ConfiguracionDistribucionPort;
import com.servify.solicitudes.application.port.out.ConfirmacionFinalizacionRepositoryPort;
import com.servify.solicitudes.application.port.out.ContraofertaRepositoryPort;
import com.servify.solicitudes.application.port.out.DistribucionSolicitudRepositoryPort;
import com.servify.solicitudes.application.port.out.PublicacionesCompatiblesPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.application.service.CalificarServicioService;
import com.servify.solicitudes.application.service.ConfirmarAsignacionSolicitudService;
import com.servify.solicitudes.application.service.ConfirmarFinalizacionServicioService;
import com.servify.solicitudes.application.service.CrearSolicitudServicioService;
import com.servify.solicitudes.application.service.EmitirContraofertaService;
import com.servify.solicitudes.application.service.ResolverContraofertaService;
import com.servify.solicitudes.application.service.ResponderDistribucionSolicitudService;
import com.servify.solicitudes.domain.enumtype.EstadoAsignacion;
import com.servify.solicitudes.domain.enumtype.EstadoContraoferta;
import com.servify.solicitudes.domain.enumtype.EstadoDistribucion;
import com.servify.solicitudes.domain.enumtype.EstadoSolicitud;
import com.servify.solicitudes.domain.enumtype.RolConfirmante;
import com.servify.solicitudes.domain.model.AsignacionServicio;
import com.servify.solicitudes.domain.model.Calificacion;
import com.servify.solicitudes.domain.model.ConfirmacionFinalizacion;
import com.servify.solicitudes.domain.model.Contraoferta;
import com.servify.solicitudes.domain.model.DistribucionSolicitud;
import com.servify.solicitudes.domain.model.SolicitudServicio;
import com.servify.solicitudes.domain.service.MotorDistribucionSolicitudes;
import com.servify.solicitudes.domain.service.PoliticaAsignacionUnica;
import com.servify.solicitudes.domain.service.PoliticaCalificacion;
import com.servify.solicitudes.domain.service.PoliticaFinalizacionMutua;
import com.servify.usuarios.application.dto.ActualizarPerfilUsuarioCommand;
import com.servify.usuarios.application.dto.CrearUsuarioCommand;
import com.servify.usuarios.application.dto.PerfilUsuarioResult;
import com.servify.usuarios.application.dto.UsuarioResult;
import com.servify.usuarios.application.port.out.PerfilUsuarioRepositoryPort;
import com.servify.usuarios.application.port.out.UsuarioRepositoryPort;
import com.servify.usuarios.application.service.ActualizarPerfilUsuarioService;
import com.servify.usuarios.application.service.CrearUsuarioService;
import com.servify.usuarios.domain.enumtype.EstadoUsuario;
import com.servify.usuarios.domain.enumtype.EstadoValidacionIdentidad;
import com.servify.usuarios.domain.enumtype.Rol;
import com.servify.usuarios.domain.model.PerfilUsuario;
import com.servify.usuarios.domain.model.Usuario;
import com.servify.usuarios.domain.service.PoliticaPerfilCompleto;
import com.servify.usuarios.domain.valueobject.Contacto;
import com.servify.usuarios.domain.valueobject.NombreCompleto;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MvpApplicationFlowTests {

    @Test
    void flujoPrincipalMvp_conAceptacionDirecta_finalizaYCalificaServicio() {
        TestContext ctx = new TestContext();
        Ubicacion ubicacion = ubicacionCaba();
        DisponibilidadHoraria disponibilidad = disponibilidadLunes();

        UsuarioResult solicitante = ctx.crearUsuario.crear(
                new CrearUsuarioCommand("cliente@servify.test", "1111", Rol.USUARIO));
        UsuarioResult prestador = ctx.crearUsuario.crear(
                new CrearUsuarioCommand("plomero@servify.test", "2222", Rol.USUARIO));

        PerfilUsuarioResult perfilPrestador = ctx.actualizarPerfil.actualizar(
                new ActualizarPerfilUsuarioCommand(
                        prestador.getId(),
                        "Ana",
                        "Gomez",
                        32,
                        "https://cdn.servify.test/ana.jpg",
                        ubicacion,
                        "Plomeria general"
                ));
        assertTrue(perfilPrestador.getPerfilCompleto());

        CategoriaServicioResult categoria = ctx.crearCategoria.crear(
                new CrearCategoriaServicioCommand("Plomeria", "Arreglos domesticos"));
        categoria = ctx.cambiarEstadoCategoria.cambiarEstado(
                new CambiarEstadoCategoriaServicioCommand(categoria.getId(), EstadoCategoria.ACTIVA, "MVP"));

        PublicacionServicioResult publicacion = ctx.crearPublicacion.crear(
                new CrearPublicacionCommand(
                        prestador.getId(),
                        categoria.getId(),
                        "Plomera a domicilio",
                        "Reparaciones y mantenimiento",
                        ModalidadServicio.PRESENCIAL,
                        ubicacion,
                        List.of(disponibilidad),
                        BigDecimal.valueOf(1000)
                ));
        publicacion = ctx.cambiarEstadoPublicacion.cambiarEstado(
                new CambiarEstadoPublicacionCommand(
                        publicacion.getId(),
                        prestador.getId(),
                        EstadoPublicacion.ACTIVA,
                        "Lista para recibir solicitudes"
                ));
        assertTrue(publicacion.getPuedeParticiparEnDistribucion());

        SolicitudServicioResult solicitud = ctx.crearSolicitud.crear(
                new CrearSolicitudServicioCommand(
                        solicitante.getId(),
                        categoria.getId(),
                        ModalidadServicio.PRESENCIAL,
                        ubicacion,
                        disponibilidad,
                        "Necesito reparar una perdida",
                        BigDecimal.valueOf(1500)
                ));

        List<DistribucionSolicitud> recibidas = ctx.distribuciones.buscarPorPrestadorId(prestador.getId());
        assertEquals(1, recibidas.size());
        DistribucionSolicitud distribucion = recibidas.get(0);
        assertEquals(EstadoDistribucion.ENVIADA, distribucion.getEstado());

        ctx.responderDistribucion.responder(
                new ResponderDistribucionSolicitudCommand(
                        distribucion.getId(),
                        prestador.getId(),
                        TipoRespuestaDistribucion.ACEPTAR
                ));
        assertEquals(EstadoDistribucion.ACEPTADA, distribucion.getEstado());

        AsignacionServicioResult asignacion = ctx.confirmarAsignacion.confirmar(
                new ConfirmarAsignacionSolicitudCommand(
                        solicitud.getId(),
                        distribucion.getId(),
                        solicitante.getId()
                ));
        assertEquals(EstadoAsignacion.ACTIVA, asignacion.getEstado());
        assertEquals(EstadoSolicitud.ASIGNADA, ctx.solicitudes.buscarPorId(solicitud.getId()).orElseThrow().getEstado());

        ctx.confirmarFinalizacion.confirmar(
                new ConfirmarFinalizacionServicioCommand(
                        solicitud.getId(),
                        asignacion.getId(),
                        solicitante.getId(),
                        RolConfirmante.SOLICITANTE,
                        "Trabajo recibido"
                ));
        assertEquals(EstadoSolicitud.ASIGNADA, ctx.solicitudes.buscarPorId(solicitud.getId()).orElseThrow().getEstado());

        ctx.confirmarFinalizacion.confirmar(
                new ConfirmarFinalizacionServicioCommand(
                        solicitud.getId(),
                        asignacion.getId(),
                        prestador.getId(),
                        RolConfirmante.PRESTADOR,
                        "Trabajo realizado"
                ));
        assertEquals(EstadoSolicitud.FINALIZADA, ctx.solicitudes.buscarPorId(solicitud.getId()).orElseThrow().getEstado());

        ctx.calificar.calificar(
                new CalificarServicioCommand(
                        solicitud.getId(),
                        asignacion.getId(),
                        solicitante.getId(),
                        prestador.getId(),
                        5
                ));

        Calificacion calificacion = ctx.calificaciones.buscarPorSolicitudId(solicitud.getId()).orElseThrow();
        assertEquals(5, calificacion.getPuntaje());
        assertEquals(prestador.getId(), calificacion.getPrestadorId());
    }

    @Test
    void contraofertaAceptada_dejaDistribucionAceptadaYConfirmable() {
        TestContext ctx = new TestContext();
        UUID solicitanteId = UUID.randomUUID();
        UUID prestadorId = UUID.randomUUID();
        UUID categoriaId = UUID.randomUUID();
        SolicitudServicio solicitud = solicitud(solicitanteId, categoriaId, BigDecimal.valueOf(1200));
        DistribucionSolicitud distribucion = distribucion(solicitud.getId(), UUID.randomUUID(), prestadorId);

        ctx.solicitudes.guardar(solicitud);
        ctx.distribuciones.guardar(distribucion);

        ctx.emitirContraoferta.emitir(
                new EmitirContraofertaCommand(
                        distribucion.getId(),
                        prestadorId,
                        BigDecimal.valueOf(1400),
                        "Incluye materiales"
                ));

        Contraoferta contraoferta = ctx.contraofertas
                .buscarPorDistribucionSolicitudId(distribucion.getId())
                .get(0);
        assertEquals(EstadoDistribucion.CONTRAOFERTADA, distribucion.getEstado());

        ContraofertaResult result = ctx.resolverContraoferta.resolver(
                new ResolverContraofertaCommand(
                        contraoferta.getId(),
                        solicitanteId,
                        TipoDecisionSolicitud.ACEPTAR
                ));

        assertEquals(EstadoContraoferta.ACEPTADA, result.getEstado());
        assertEquals(EstadoDistribucion.ACEPTADA, distribucion.getEstado());
        assertEquals(
                BigDecimal.valueOf(1400),
                ctx.solicitudes.buscarPorId(solicitud.getId()).orElseThrow().getPrecioReferencia()
        );
    }

    @Test
    void loginFallido_persisteIntentoFallidoAntesDeRechazar() {
        TestContext ctx = new TestContext();
        UsuarioResult usuario = ctx.crearUsuario.crear(
                new CrearUsuarioCommand("login@servify.test", null, Rol.USUARIO));
        ctx.registrarCredenciales.registrar(
                new RegistrarCredencialesCommand(usuario.getId(), "login@servify.test", "secreta"));

        assertThrows(IllegalArgumentException.class,
                () -> ctx.iniciarSesion.iniciar(new IniciarSesionCommand("login@servify.test", "incorrecta")));

        CredencialAcceso credencial = ctx.credenciales.buscarPorEmailAcceso("login@servify.test").orElseThrow();
        assertEquals(1, credencial.getIntentosFallidos());
        assertEquals(2, ctx.credenciales.guardados);
    }

    @Test
    void moderacion_aceptaEstadosRealesDePublicacion() {
        UUID adminId = UUID.randomUUID();
        UUID publicacionId = UUID.randomUUID();
        UsuarioAdministrableFake usuarios = new UsuarioAdministrableFake(adminId);
        PublicacionModerableFake publicaciones = new PublicacionModerableFake(publicacionId);
        ModerarPublicacionService service = new ModerarPublicacionService(publicaciones, usuarios);

        service.moderar(new ModerarPublicacionCommand(publicacionId, adminId, "BLOQUEADA", "Incumple reglas"));

        assertEquals("BLOQUEADA", publicaciones.estadoDestino);
        assertEquals("Incumple reglas", publicaciones.motivo);
    }

    @Test
    void publicacionInactiva_conDatosValidosPuedeActivarse() {
        CategoriaServicio categoria = new CategoriaServicio(
                UUID.randomUUID(),
                "Electricidad",
                "Servicios electricos",
                EstadoCategoria.ACTIVA
        );
        PublicacionServicio publicacion = new PublicacionServicio(
                UUID.randomUUID(),
                UUID.randomUUID(),
                categoria,
                "Electricista",
                "Instalaciones",
                ModalidadServicio.PRESENCIAL,
                ubicacionCaba(),
                List.of(disponibilidadLunes()),
                BigDecimal.valueOf(2000),
                EstadoPublicacion.INACTIVA
        );

        assertFalse(publicacion.puedeParticiparEnDistribucion());
        publicacion.activar();

        assertEquals(EstadoPublicacion.ACTIVA, publicacion.getEstado());
        assertTrue(publicacion.puedeParticiparEnDistribucion());
    }

    @Test
    void usuarioConPerfilLegacyCompletoPeroBanderaFalsePuedePublicar() {
        UUID usuarioId = UUID.randomUUID();
        PerfilUsuario perfil = new PerfilUsuario(
                UUID.randomUUID(),
                usuarioId,
                new NombreCompleto("Lucia", "Paredes"),
                29,
                null,
                ubicacionCaba(),
                "Perfil migrado desde datos previos",
                false
        );
        Usuario usuario = new Usuario(
                usuarioId,
                "legacy.publicador",
                new Contacto("legacy-publicador@servify.test", null),
                Rol.USUARIO,
                EstadoUsuario.ACTIVO,
                EstadoValidacionIdentidad.NO_REQUERIDA,
                perfil,
                LocalDateTime.now()
        );

        assertTrue(perfil.estaCompleto());
        assertFalse(perfil.getPerfilCompleto());
        assertTrue(usuario.puedePublicarServicios());
    }

    private static Ubicacion ubicacionCaba() {
        return new Ubicacion(
                "Argentina",
                "Buenos Aires",
                "CABA",
                "Palermo",
                "Av. Santa Fe",
                "1234",
                null,
                -34.5889,
                -58.4306
        );
    }

    private static DisponibilidadHoraria disponibilidadLunes() {
        return new DisponibilidadHoraria(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0));
    }

    private static SolicitudServicio solicitud(UUID solicitanteId, UUID categoriaId, BigDecimal precioReferencia) {
        return new SolicitudServicio(
                UUID.randomUUID(),
                solicitanteId,
                categoriaId,
                ModalidadServicio.PRESENCIAL,
                ubicacionCaba(),
                disponibilidadLunes(),
                "Solicitud de prueba",
                precioReferencia,
                EstadoSolicitud.BUSCANDO_PRESTADOR,
                LocalDateTime.now()
        );
    }

    private static DistribucionSolicitud distribucion(UUID solicitudId, UUID publicacionId, UUID prestadorId) {
        return new DistribucionSolicitud(
                UUID.randomUUID(),
                solicitudId,
                publicacionId,
                prestadorId,
                EstadoDistribucion.ENVIADA,
                1,
                LocalDateTime.now(),
                null,
                LocalDateTime.now().plusMinutes(30)
        );
    }

    private static class TestContext {
        final UsuarioRepo usuarios = new UsuarioRepo();
        final PerfilRepo perfiles = new PerfilRepo();
        final CategoriaRepo categorias = new CategoriaRepo();
        final PublicacionRepo publicaciones = new PublicacionRepo();
        final SolicitudRepo solicitudes = new SolicitudRepo();
        final DistribucionRepo distribuciones = new DistribucionRepo();
        final AsignacionRepo asignaciones = new AsignacionRepo();
        final ConfirmacionRepo confirmaciones = new ConfirmacionRepo();
        final CalificacionRepo calificaciones = new CalificacionRepo();
        final ContraofertaRepo contraofertas = new ContraofertaRepo();
        final CredencialRepo credenciales = new CredencialRepo();
        final RefreshTokenRepo refreshTokens = new RefreshTokenRepo();
        final SimplePasswordHasher passwordHasher = new SimplePasswordHasher();
        final SimpleTokenProvider tokenProvider = new SimpleTokenProvider();
        final UsuarioAutenticableAdapter usuarioAutenticable = new UsuarioAutenticableAdapter(usuarios);

        final CrearUsuarioService crearUsuario = new CrearUsuarioService(usuarios);
        final ActualizarPerfilUsuarioService actualizarPerfil = new ActualizarPerfilUsuarioService(
                usuarios,
                perfiles,
                new PoliticaPerfilCompleto()
        );
        final CrearCategoriaServicioService crearCategoria = new CrearCategoriaServicioService(categorias);
        final CambiarEstadoCategoriaServicioService cambiarEstadoCategoria =
                new CambiarEstadoCategoriaServicioService(categorias);
        final CrearPublicacionService crearPublicacion = new CrearPublicacionService(
                publicaciones,
                categorias,
                new UsuarioPublicadorAdapter(usuarios),
                new ValidadorDisponibilidadHoraria()
        );
        final CambiarEstadoPublicacionService cambiarEstadoPublicacion =
                new CambiarEstadoPublicacionService(publicaciones);
        final CrearSolicitudServicioService crearSolicitud = new CrearSolicitudServicioService(
                solicitudes,
                distribuciones,
                new PublicacionesCompatiblesAdapter(publicaciones),
                new ConfiguracionDistribucionFake(),
                new MotorDistribucionSolicitudes()
        );
        final ResponderDistribucionSolicitudService responderDistribucion =
                new ResponderDistribucionSolicitudService(distribuciones, solicitudes);
        final ConfirmarAsignacionSolicitudService confirmarAsignacion = new ConfirmarAsignacionSolicitudService(
                solicitudes,
                distribuciones,
                asignaciones,
                new PoliticaAsignacionUnica()
        );
        final ConfirmarFinalizacionServicioService confirmarFinalizacion = new ConfirmarFinalizacionServicioService(
                confirmaciones,
                asignaciones,
                solicitudes,
                new PoliticaFinalizacionMutua()
        );
        final CalificarServicioService calificar = new CalificarServicioService(
                calificaciones,
                solicitudes,
                asignaciones,
                new PoliticaCalificacion()
        );
        final EmitirContraofertaService emitirContraoferta = new EmitirContraofertaService(
                contraofertas,
                distribuciones,
                solicitudes
        );
        final ResolverContraofertaService resolverContraoferta = new ResolverContraofertaService(
                contraofertas,
                distribuciones,
                solicitudes
        );
        final RegistrarCredencialesService registrarCredenciales = new RegistrarCredencialesService(
                credenciales,
                passwordHasher,
                usuarioAutenticable
        );
        final IniciarSesionService iniciarSesion = new IniciarSesionService(
                credenciales,
                passwordHasher,
                tokenProvider,
                refreshTokens,
                usuarioAutenticable
        );
    }

    private static class UsuarioRepo implements UsuarioRepositoryPort {
        private final Map<UUID, Usuario> usuarios = new LinkedHashMap<>();

        @Override
        public Usuario guardar(Usuario usuario) {
            usuarios.put(usuario.getId(), usuario);
            return usuario;
        }

        @Override
        public Optional<Usuario> buscarPorId(UUID usuarioId) {
            return Optional.ofNullable(usuarios.get(usuarioId));
        }

        @Override
        public Optional<Usuario> buscarPorEmail(String email) {
            return usuarios.values().stream()
                    .filter(usuario -> usuario.getContacto() != null)
                    .filter(usuario -> usuario.getContacto().getEmail().equalsIgnoreCase(email))
                    .findFirst();
        }

        @Override
        public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) {
            return usuarios.values().stream()
                    .filter(usuario -> usuario.getNombreUsuario() != null)
                    .filter(usuario -> usuario.getNombreUsuario().equalsIgnoreCase(nombreUsuario))
                    .findFirst();
        }

        @Override
        public boolean existePorEmail(String email) {
            return buscarPorEmail(email).isPresent();
        }

        @Override
        public boolean existePorNombreUsuario(String nombreUsuario) {
            return buscarPorNombreUsuario(nombreUsuario).isPresent();
        }

        @Override
        public List<Usuario> listarPorEstado(com.servify.usuarios.domain.enumtype.EstadoUsuario estado) {
            return usuarios.values().stream()
                    .filter(usuario -> usuario.getEstado() == estado)
                    .toList();
        }
    }

    private static class PerfilRepo implements PerfilUsuarioRepositoryPort {
        private final Map<UUID, PerfilUsuario> perfiles = new LinkedHashMap<>();

        @Override
        public PerfilUsuario guardar(PerfilUsuario perfilUsuario) {
            perfiles.put(perfilUsuario.getId(), perfilUsuario);
            return perfilUsuario;
        }

        @Override
        public Optional<PerfilUsuario> buscarPorId(UUID perfilUsuarioId) {
            return Optional.ofNullable(perfiles.get(perfilUsuarioId));
        }

        @Override
        public Optional<PerfilUsuario> buscarPorUsuarioId(UUID usuarioId) {
            return perfiles.values().stream()
                    .filter(perfil -> Objects.equals(perfil.getUsuarioId(), usuarioId))
                    .findFirst();
        }
    }

    private static class CategoriaRepo implements CategoriaServicioRepositoryPort {
        private final Map<UUID, CategoriaServicio> categorias = new LinkedHashMap<>();

        @Override
        public CategoriaServicio guardar(CategoriaServicio categoriaServicio) {
            categorias.put(categoriaServicio.getId(), categoriaServicio);
            return categoriaServicio;
        }

        @Override
        public Optional<CategoriaServicio> buscarPorId(UUID categoriaServicioId) {
            return Optional.ofNullable(categorias.get(categoriaServicioId));
        }

        @Override
        public Optional<CategoriaServicio> buscarPorNombre(String nombre) {
            return categorias.values().stream()
                    .filter(categoria -> categoria.getNombre().equalsIgnoreCase(nombre))
                    .findFirst();
        }

        @Override
        public List<CategoriaServicio> listarTodas() {
            return List.copyOf(categorias.values());
        }

        @Override
        public List<CategoriaServicio> listarActivas() {
            return categorias.values().stream()
                    .filter(CategoriaServicio::estaActiva)
                    .toList();
        }

        @Override
        public boolean existePorNombre(String nombre) {
            return buscarPorNombre(nombre).isPresent();
        }
    }

    private static class PublicacionRepo implements PublicacionServicioRepositoryPort {
        private final Map<UUID, PublicacionServicio> publicaciones = new LinkedHashMap<>();

        @Override
        public PublicacionServicio guardar(PublicacionServicio publicacionServicio) {
            publicaciones.put(publicacionServicio.getId(), publicacionServicio);
            return publicacionServicio;
        }

        @Override
        public Optional<PublicacionServicio> buscarPorId(UUID publicacionServicioId) {
            return Optional.ofNullable(publicaciones.get(publicacionServicioId));
        }

        @Override
        public List<PublicacionServicio> buscarPorUsuarioId(UUID usuarioId) {
            return publicaciones.values().stream()
                    .filter(publicacion -> Objects.equals(publicacion.getUsuarioId(), usuarioId))
                    .toList();
        }

        @Override
        public List<PublicacionServicio> buscarActivas() {
            return publicaciones.values().stream()
                    .filter(PublicacionServicio::estaActiva)
                    .toList();
        }

        @Override
        public List<PublicacionServicio> buscarActivasPorCategoria(UUID categoriaServicioId) {
            return publicaciones.values().stream()
                    .filter(PublicacionServicio::estaActiva)
                    .filter(publicacion -> publicacion.getCategoriaServicio() != null)
                    .filter(publicacion -> Objects.equals(publicacion.getCategoriaServicio().getId(), categoriaServicioId))
                    .toList();
        }

        @Override
        public boolean existePorUsuarioIdYTitulo(UUID usuarioId, String titulo) {
            return publicaciones.values().stream()
                    .filter(publicacion -> Objects.equals(publicacion.getUsuarioId(), usuarioId))
                    .anyMatch(publicacion -> publicacion.getTitulo() != null
                            && publicacion.getTitulo().equalsIgnoreCase(titulo));
        }
    }

    private static class SolicitudRepo implements SolicitudServicioRepositoryPort {
        private final Map<UUID, SolicitudServicio> solicitudes = new LinkedHashMap<>();

        @Override
        public SolicitudServicio guardar(SolicitudServicio solicitudServicio) {
            solicitudes.put(solicitudServicio.getId(), solicitudServicio);
            return solicitudServicio;
        }

        @Override
        public Optional<SolicitudServicio> buscarPorId(UUID solicitudId) {
            return Optional.ofNullable(solicitudes.get(solicitudId));
        }

        @Override
        public List<SolicitudServicio> buscarPorSolicitanteId(UUID solicitanteId) {
            return solicitudes.values().stream()
                    .filter(solicitud -> Objects.equals(solicitud.getSolicitanteId(), solicitanteId))
                    .toList();
        }
    }

    private static class DistribucionRepo implements DistribucionSolicitudRepositoryPort {
        private final Map<UUID, DistribucionSolicitud> distribuciones = new LinkedHashMap<>();

        @Override
        public DistribucionSolicitud guardar(DistribucionSolicitud distribucionSolicitud) {
            distribuciones.put(distribucionSolicitud.getId(), distribucionSolicitud);
            return distribucionSolicitud;
        }

        @Override
        public Optional<DistribucionSolicitud> buscarPorId(UUID distribucionSolicitudId) {
            return Optional.ofNullable(distribuciones.get(distribucionSolicitudId));
        }

        @Override
        public List<DistribucionSolicitud> buscarPorSolicitudId(UUID solicitudId) {
            return distribuciones.values().stream()
                    .filter(distribucion -> Objects.equals(distribucion.getSolicitudId(), solicitudId))
                    .toList();
        }

        @Override
        public List<DistribucionSolicitud> buscarPorPrestadorId(UUID prestadorId) {
            return distribuciones.values().stream()
                    .filter(distribucion -> Objects.equals(distribucion.getPrestadorId(), prestadorId))
                    .toList();
        }

        @Override
        public List<DistribucionSolicitud> buscarActivasPorSolicitudId(UUID solicitudId) {
            return buscarPorSolicitudId(solicitudId).stream()
                    .filter(distribucion -> distribucion.estaEnviada()
                            || distribucion.estaContraofertada()
                            || distribucion.estaAceptada())
                    .toList();
        }
    }

    private static class AsignacionRepo implements AsignacionServicioRepositoryPort {
        private final Map<UUID, AsignacionServicio> asignaciones = new LinkedHashMap<>();

        @Override
        public AsignacionServicio guardar(AsignacionServicio asignacionServicio) {
            asignaciones.put(asignacionServicio.getId(), asignacionServicio);
            return asignacionServicio;
        }

        @Override
        public Optional<AsignacionServicio> buscarPorId(UUID asignacionServicioId) {
            return Optional.ofNullable(asignaciones.get(asignacionServicioId));
        }

        @Override
        public Optional<AsignacionServicio> buscarPorSolicitudId(UUID solicitudId) {
            return asignaciones.values().stream()
                    .filter(asignacion -> Objects.equals(asignacion.getSolicitudId(), solicitudId))
                    .findFirst();
        }

        @Override
        public List<AsignacionServicio> buscarPorPrestadorId(UUID prestadorId) {
            return asignaciones.values().stream()
                    .filter(asignacion -> Objects.equals(asignacion.getPrestadorId(), prestadorId))
                    .toList();
        }

        @Override
        public List<AsignacionServicio> buscarPorSolicitanteId(UUID solicitanteId) {
            return List.of();
        }
    }

    private static class ConfirmacionRepo implements ConfirmacionFinalizacionRepositoryPort {
        private final Map<UUID, ConfirmacionFinalizacion> confirmaciones = new LinkedHashMap<>();

        @Override
        public ConfirmacionFinalizacion guardar(ConfirmacionFinalizacion confirmacionFinalizacion) {
            confirmaciones.put(confirmacionFinalizacion.getId(), confirmacionFinalizacion);
            return confirmacionFinalizacion;
        }

        @Override
        public Optional<ConfirmacionFinalizacion> buscarPorId(UUID confirmacionFinalizacionId) {
            return Optional.ofNullable(confirmaciones.get(confirmacionFinalizacionId));
        }

        @Override
        public List<ConfirmacionFinalizacion> buscarPorSolicitudId(UUID solicitudId) {
            return confirmaciones.values().stream()
                    .filter(confirmacion -> Objects.equals(confirmacion.getSolicitudId(), solicitudId))
                    .toList();
        }

        @Override
        public List<ConfirmacionFinalizacion> buscarPorAsignacionServicioId(UUID asignacionServicioId) {
            return confirmaciones.values().stream()
                    .filter(confirmacion -> Objects.equals(confirmacion.getAsignacionServicioId(), asignacionServicioId))
                    .toList();
        }

        @Override
        public List<ConfirmacionFinalizacion> buscarPorEncuentroServicioId(UUID encuentroServicioId) {
            return confirmaciones.values().stream()
                    .filter(confirmacion -> Objects.equals(confirmacion.getEncuentroServicioId(), encuentroServicioId))
                    .toList();
        }

        @Override
        public Optional<ConfirmacionFinalizacion> buscarPorAsignacionServicioIdYRolConfirmante(
                UUID asignacionServicioId,
                RolConfirmante rolConfirmante
        ) {
            return confirmaciones.values().stream()
                    .filter(confirmacion -> Objects.equals(confirmacion.getAsignacionServicioId(), asignacionServicioId))
                    .filter(confirmacion -> confirmacion.getRolConfirmante() == rolConfirmante)
                    .findFirst();
        }

        @Override
        public Optional<ConfirmacionFinalizacion> buscarPorEncuentroServicioIdYRolConfirmante(
                UUID encuentroServicioId,
                RolConfirmante rolConfirmante
        ) {
            return confirmaciones.values().stream()
                    .filter(confirmacion -> Objects.equals(confirmacion.getEncuentroServicioId(), encuentroServicioId))
                    .filter(confirmacion -> confirmacion.getRolConfirmante() == rolConfirmante)
                    .findFirst();
        }

        @Override
        public List<ConfirmacionFinalizacion> buscarPorConfirmanteId(UUID confirmanteId) {
            return confirmaciones.values().stream()
                    .filter(confirmacion -> Objects.equals(confirmacion.getConfirmanteId(), confirmanteId))
                    .toList();
        }
    }

    private static class CalificacionRepo implements CalificacionRepositoryPort {
        private final Map<UUID, Calificacion> calificaciones = new LinkedHashMap<>();

        @Override
        public Calificacion guardar(Calificacion calificacion) {
            calificaciones.put(calificacion.getId(), calificacion);
            return calificacion;
        }

        @Override
        public Optional<Calificacion> buscarPorId(UUID calificacionId) {
            return Optional.ofNullable(calificaciones.get(calificacionId));
        }

        @Override
        public Optional<Calificacion> buscarPorSolicitudId(UUID solicitudId) {
            return calificaciones.values().stream()
                    .filter(calificacion -> Objects.equals(calificacion.getSolicitudId(), solicitudId))
                    .findFirst();
        }

        @Override
        public Optional<Calificacion> buscarPorAsignacionServicioIdYRolCalificador(
                UUID asignacionServicioId,
                com.servify.solicitudes.domain.enumtype.RolConfirmante rolCalificador
        ) {
            return calificaciones.values().stream()
                    .filter(calificacion -> Objects.equals(calificacion.getAsignacionServicioId(), asignacionServicioId))
                    .filter(calificacion -> Objects.equals(calificacion.getRolCalificador(), rolCalificador))
                    .findFirst();
        }

        @Override
        public List<Calificacion> buscarPorPrestadorId(UUID prestadorId) {
            return calificaciones.values().stream()
                    .filter(calificacion -> Objects.equals(calificacion.getCalificadoId(), prestadorId))
                    .toList();
        }

        @Override
        public List<Calificacion> buscarPorSolicitanteId(UUID solicitanteId) {
            return calificaciones.values().stream()
                    .filter(calificacion -> Objects.equals(calificacion.getCalificadorId(), solicitanteId))
                    .toList();
        }
    }

    private static class ContraofertaRepo implements ContraofertaRepositoryPort {
        private final Map<UUID, Contraoferta> contraofertas = new LinkedHashMap<>();

        @Override
        public Contraoferta guardar(Contraoferta contraoferta) {
            contraofertas.put(contraoferta.getId(), contraoferta);
            return contraoferta;
        }

        @Override
        public Optional<Contraoferta> buscarPorId(UUID contraofertaId) {
            return Optional.ofNullable(contraofertas.get(contraofertaId));
        }

        @Override
        public List<Contraoferta> buscarPorDistribucionSolicitudId(UUID distribucionSolicitudId) {
            return contraofertas.values().stream()
                    .filter(contraoferta -> Objects.equals(contraoferta.getDistribucionSolicitudId(), distribucionSolicitudId))
                    .toList();
        }

        @Override
        public Optional<Contraoferta> buscarPendientePorDistribucionSolicitudId(UUID distribucionSolicitudId) {
            return buscarPorDistribucionSolicitudId(distribucionSolicitudId).stream()
                    .filter(Contraoferta::estaPendiente)
                    .findFirst();
        }

        @Override
        public List<Contraoferta> buscarPorPrestadorId(UUID prestadorId) {
            return contraofertas.values().stream()
                    .filter(contraoferta -> Objects.equals(contraoferta.getPrestadorId(), prestadorId))
                    .toList();
        }
    }

    private static class CredencialRepo implements CredencialAccesoRepositoryPort {
        private final Map<UUID, CredencialAcceso> credenciales = new LinkedHashMap<>();
        private int guardados;

        @Override
        public CredencialAcceso guardar(CredencialAcceso credencialAcceso) {
            credenciales.put(credencialAcceso.getId(), credencialAcceso);
            guardados++;
            return credencialAcceso;
        }

        @Override
        public Optional<CredencialAcceso> buscarPorId(UUID credencialAccesoId) {
            return Optional.ofNullable(credenciales.get(credencialAccesoId));
        }

        @Override
        public Optional<CredencialAcceso> buscarPorUsuarioId(UUID usuarioId) {
            return credenciales.values().stream()
                    .filter(credencial -> Objects.equals(credencial.getUsuarioId(), usuarioId))
                    .findFirst();
        }

        @Override
        public Optional<CredencialAcceso> buscarPorEmailAcceso(String emailAcceso) {
            return credenciales.values().stream()
                    .filter(credencial -> credencial.usaEmail(emailAcceso))
                    .findFirst();
        }

        @Override
        public boolean existePorEmailAcceso(String emailAcceso) {
            return buscarPorEmailAcceso(emailAcceso).isPresent();
        }
    }

    private static class RefreshTokenRepo implements RefreshTokenRepositoryPort {
        private final Map<UUID, RefreshToken> refreshTokens = new LinkedHashMap<>();

        @Override
        public RefreshToken guardar(RefreshToken refreshToken) {
            refreshTokens.put(refreshToken.getId(), refreshToken);
            return refreshToken;
        }

        @Override
        public Optional<RefreshToken> buscarPorId(UUID refreshTokenId) {
            return Optional.ofNullable(refreshTokens.get(refreshTokenId));
        }

        @Override
        public Optional<RefreshToken> buscarPorTokenHash(String tokenHash) {
            return refreshTokens.values().stream()
                    .filter(refreshToken -> Objects.equals(refreshToken.getTokenHash(), tokenHash))
                    .findFirst();
        }

        @Override
        public List<RefreshToken> buscarActivosPorUsuarioId(UUID usuarioId) {
            return refreshTokens.values().stream()
                    .filter(refreshToken -> Objects.equals(refreshToken.getUsuarioId(), usuarioId))
                    .filter(RefreshToken::estaActivo)
                    .toList();
        }
    }

    private static class UsuarioPublicadorAdapter implements UsuarioPublicadorPort {
        private final UsuarioRepo usuarios;

        private UsuarioPublicadorAdapter(UsuarioRepo usuarios) {
            this.usuarios = usuarios;
        }

        @Override
        public boolean existeUsuario(UUID usuarioId) {
            return usuarios.buscarPorId(usuarioId).isPresent();
        }

        @Override
        public boolean puedePublicarServicios(UUID usuarioId) {
            return usuarios.buscarPorId(usuarioId)
                    .map(Usuario::puedePublicarServicios)
                    .orElse(false);
        }
    }

    private static class PublicacionesCompatiblesAdapter implements PublicacionesCompatiblesPort {
        private final PublicacionRepo publicaciones;
        private final PoliticaCompatibilidadPublicacion politica = new PoliticaCompatibilidadPublicacion();

        private PublicacionesCompatiblesAdapter(PublicacionRepo publicaciones) {
            this.publicaciones = publicaciones;
        }

        @Override
        public Map<UUID, UUID> buscarPublicacionesCompatibles(UUID solicitudId,
                                                              UUID categoriaServicioId,
                                                              String descripcionNecesidad,
                                                              ModalidadServicio modalidadRequerida,
                                                              Ubicacion ubicacionRequerida,
                                                              DisponibilidadHoraria disponibilidadRequerida,
                                                              BigDecimal precioMaximo,
                                                              Integer radioBusquedaKm) {
            Map<UUID, UUID> compatibles = new LinkedHashMap<>();
            for (PublicacionServicio publicacion : publicaciones.buscarActivasPorCategoria(categoriaServicioId)) {
                if (politica.esCompatible(
                        publicacion,
                        publicacion.getCategoriaServicio(),
                        modalidadRequerida,
                        ubicacionRequerida,
                        disponibilidadRequerida)) {
                    compatibles.put(publicacion.getId(), publicacion.getUsuarioId());
                }
            }
            return compatibles;
        }
    }

    private static class ConfiguracionDistribucionFake implements ConfiguracionDistribucionPort {
        @Override
        public Integer obtenerRadioBusquedaInicialKm() {
            return 10;
        }

        @Override
        public Integer obtenerRadioBusquedaExpansionKm() {
            return 25;
        }

        @Override
        public Integer obtenerTiempoEsperaExpansionMinutos() {
            return 30;
        }

        @Override
        public BigDecimal obtenerPrecioBaseMinimoReferencia() {
            return BigDecimal.ZERO;
        }
    }

    private static class UsuarioAutenticableAdapter implements UsuarioAutenticablePort {
        private final UsuarioRepo usuarios;

        private UsuarioAutenticableAdapter(UsuarioRepo usuarios) {
            this.usuarios = usuarios;
        }

        @Override
        public boolean existeUsuario(UUID usuarioId) {
            return usuarios.buscarPorId(usuarioId).isPresent();
        }

        @Override
        public boolean puedeAutenticarse(UUID usuarioId) {
            return usuarios.buscarPorId(usuarioId)
                    .map(Usuario::estaActivo)
                    .orElse(false);
        }

        @Override
        public boolean coincideEmailPrincipal(UUID usuarioId, String emailAcceso) {
            return usuarios.buscarPorId(usuarioId)
                    .map(Usuario::getContacto)
                    .map(contacto -> contacto.getEmail().equalsIgnoreCase(emailAcceso))
                    .orElse(false);
        }

        @Override
        public Optional<UUID> buscarUsuarioIdPorNombreUsuario(String nombreUsuario) {
            return usuarios.buscarPorNombreUsuario(nombreUsuario).map(Usuario::getId);
        }
    }

    private static class SimplePasswordHasher implements PasswordHasherPort {
        @Override
        public String hashear(String passwordPlano) {
            return "hash:" + passwordPlano;
        }

        @Override
        public boolean coincide(String passwordPlano, String passwordHash) {
            return Objects.equals(hashear(passwordPlano), passwordHash);
        }
    }

    private static class SimpleTokenProvider implements TokenProviderPort {
        @Override
        public TokenResult generarAccessToken(UUID usuarioId, String emailAcceso) {
            return new TokenResult(
                    "access:" + usuarioId,
                    "Bearer",
                    LocalDateTime.now(),
                    LocalDateTime.now().plusMinutes(15)
            );
        }

        @Override
        public TokenResult generarRefreshToken(UUID usuarioId, String emailAcceso) {
            return new TokenResult(
                    "refresh:" + usuarioId,
                    "Refresh",
                    LocalDateTime.now(),
                    LocalDateTime.now().plusDays(7)
            );
        }

        @Override
        public String obtenerHashToken(String token) {
            return "token-hash:" + token;
        }
    }

    private static class UsuarioAdministrableFake implements UsuarioAdministrablePort {
        private final UUID adminId;

        private UsuarioAdministrableFake(UUID adminId) {
            this.adminId = adminId;
        }

        @Override
        public boolean existeUsuario(UUID usuarioId) {
            return Objects.equals(adminId, usuarioId);
        }

        @Override
        public boolean esAdministrador(UUID usuarioId) {
            return Objects.equals(adminId, usuarioId);
        }

        @Override
        public void aplicarMedida(UUID usuarioId, TipoMedida tipoMedida, String motivo) {
        }
    }

    private static class PublicacionModerableFake implements PublicacionModerablePort {
        private final UUID publicacionId;
        private String estadoDestino;
        private String motivo;

        private PublicacionModerableFake(UUID publicacionId) {
            this.publicacionId = publicacionId;
        }

        @Override
        public boolean existePublicacion(UUID publicacionServicioId) {
            return Objects.equals(publicacionId, publicacionServicioId);
        }

        @Override
        public void moderarPublicacion(UUID publicacionServicioId, String estadoDestino, String motivo) {
            this.estadoDestino = estadoDestino;
            this.motivo = motivo;
        }
    }
}
