package com.servify.shared.infrastructure.config;

import com.servify.administracion.application.port.in.ActualizarConfiguracionGeneralUseCase;
import com.servify.administracion.application.port.in.AplicarMedidaAdministrativaUsuarioUseCase;
import com.servify.administracion.application.port.in.ListarPublicacionesAdminUseCase;
import com.servify.administracion.application.port.in.ModerarPublicacionUseCase;
import com.servify.administracion.application.port.in.ObtenerConfiguracionGeneralUseCase;
import com.servify.administracion.application.port.in.ObtenerMedidasAdministrativasDeUsuarioUseCase;
import com.servify.administracion.application.port.out.ConfiguracionGeneralRepositoryPort;
import com.servify.administracion.application.port.out.MedidaAdministrativaUsuarioRepositoryPort;
import com.servify.administracion.application.port.out.PublicacionModerablePort;
import com.servify.administracion.application.port.out.UsuarioAdministrablePort;
import com.servify.administracion.application.service.ActualizarConfiguracionGeneralService;
import com.servify.administracion.application.service.AplicarMedidaAdministrativaUsuarioService;
import com.servify.administracion.application.service.ListarPublicacionesAdminService;
import com.servify.administracion.application.service.ModerarPublicacionService;
import com.servify.administracion.application.service.ObtenerConfiguracionGeneralService;
import com.servify.administracion.application.service.ObtenerMedidasAdministrativasDeUsuarioService;
import com.servify.autenticacion.application.port.in.CerrarSesionUseCase;
import com.servify.autenticacion.application.port.in.AutenticarConIdentidadExternaUseCase;
import com.servify.autenticacion.application.port.in.IniciarSesionUseCase;
import com.servify.autenticacion.application.port.in.RegistrarCredencialesUseCase;
import com.servify.autenticacion.application.port.in.RestablecerPasswordUseCase;
import com.servify.autenticacion.application.port.in.RenovarTokenUseCase;
import com.servify.autenticacion.application.port.in.SolicitarRecuperacionPasswordUseCase;
import com.servify.autenticacion.application.port.out.CredencialAccesoRepositoryPort;
import com.servify.autenticacion.application.port.out.IdentidadExternaRepositoryPort;
import com.servify.autenticacion.application.port.out.PasswordHasherPort;
import com.servify.autenticacion.application.port.out.PasswordResetEmailSenderPort;
import com.servify.autenticacion.application.port.out.PasswordResetTokenRepositoryPort;
import com.servify.autenticacion.application.port.out.ProveedorIdentidadVerifierPort;
import com.servify.autenticacion.application.port.out.RefreshTokenRepositoryPort;
import com.servify.autenticacion.application.port.out.TokenProviderPort;
import com.servify.autenticacion.application.port.out.UsuarioAutenticablePort;
import com.servify.autenticacion.application.service.AutenticarConIdentidadExternaService;
import com.servify.autenticacion.application.service.CerrarSesionService;
import com.servify.autenticacion.application.service.IniciarSesionService;
import com.servify.autenticacion.application.service.RegistrarCredencialesService;
import com.servify.autenticacion.application.service.RestablecerPasswordService;
import com.servify.autenticacion.application.service.RenovarTokenService;
import com.servify.autenticacion.application.service.SolicitarRecuperacionPasswordService;
import com.servify.chat.application.port.out.MensajeChatRepositoryPort;
import com.servify.chat.application.service.ChatSolicitudService;
import com.servify.notificaciones.application.port.in.CrearNotificacionUsuarioUseCase;
import com.servify.notificaciones.application.port.in.EliminarNotificacionUsuarioUseCase;
import com.servify.notificaciones.application.port.in.ListarNotificacionesUsuarioUseCase;
import com.servify.notificaciones.application.port.in.MarcarNotificacionLeidaUseCase;
import com.servify.notificaciones.application.port.out.NotificacionUsuarioRepositoryPort;
import com.servify.notificaciones.application.service.CrearNotificacionUsuarioService;
import com.servify.notificaciones.application.service.EliminarNotificacionUsuarioService;
import com.servify.notificaciones.application.service.ListarNotificacionesUsuarioService;
import com.servify.notificaciones.application.service.MarcarNotificacionLeidaService;
import com.servify.publicaciones.application.port.in.ActualizarCategoriaServicioUseCase;
import com.servify.publicaciones.application.port.in.ActualizarPublicacionUseCase;
import com.servify.publicaciones.application.port.in.BuscarPublicacionesCompatiblesUseCase;
import com.servify.publicaciones.application.port.in.CambiarEstadoCategoriaServicioUseCase;
import com.servify.publicaciones.application.port.in.CambiarEstadoPublicacionUseCase;
import com.servify.publicaciones.application.port.in.CrearCategoriaServicioUseCase;
import com.servify.publicaciones.application.port.in.CrearPublicacionUseCase;
import com.servify.publicaciones.application.port.in.ListarCategoriasActivasUseCase;
import com.servify.publicaciones.application.port.in.ListarCategoriasUseCase;
import com.servify.publicaciones.application.port.in.ListarPublicacionesPorCategoriaUseCase;
import com.servify.publicaciones.application.port.in.ListarPublicacionesDeUsuarioUseCase;
import com.servify.publicaciones.application.port.in.ObtenerPublicacionUseCase;
import com.servify.publicaciones.application.port.out.CategoriaServicioRepositoryPort;
import com.servify.publicaciones.application.port.out.PublicacionServicioRepositoryPort;
import com.servify.publicaciones.application.port.out.UsuarioPublicadorPort;
import com.servify.publicaciones.application.service.ActualizarCategoriaServicioService;
import com.servify.publicaciones.application.service.ActualizarPublicacionService;
import com.servify.publicaciones.application.service.BuscarPublicacionesCompatiblesService;
import com.servify.publicaciones.application.service.CambiarEstadoCategoriaServicioService;
import com.servify.publicaciones.application.service.CambiarEstadoPublicacionService;
import com.servify.publicaciones.application.service.CrearCategoriaServicioService;
import com.servify.publicaciones.application.service.CrearPublicacionService;
import com.servify.publicaciones.application.service.ListarCategoriasActivasService;
import com.servify.publicaciones.application.service.ListarCategoriasService;
import com.servify.publicaciones.application.service.ListarPublicacionesPorCategoriaService;
import com.servify.publicaciones.application.service.ListarPublicacionesDeUsuarioService;
import com.servify.publicaciones.application.service.ObtenerPublicacionService;
import com.servify.publicaciones.domain.service.PoliticaCompatibilidadPublicacion;
import com.servify.publicaciones.domain.service.PoliticaRelevanciaServicio;
import com.servify.publicaciones.domain.service.ValidadorDisponibilidadHoraria;
import com.servify.solicitudes.application.port.in.CalificarServicioUseCase;
import com.servify.solicitudes.application.port.in.ActualizarSolicitudServicioUseCase;
import com.servify.solicitudes.application.port.in.CancelarSolicitudServicioUseCase;
import com.servify.solicitudes.application.port.in.ConfirmarAsignacionSolicitudUseCase;
import com.servify.solicitudes.application.port.in.ConfirmarFinalizacionServicioUseCase;
import com.servify.solicitudes.application.port.in.ConsultarCalificacionServicioUseCase;
import com.servify.solicitudes.application.port.in.CrearSolicitudServicioUseCase;
import com.servify.solicitudes.application.port.in.EmitirContraofertaUseCase;
import com.servify.solicitudes.application.port.in.ListarSolicitudesDelSolicitanteUseCase;
import com.servify.solicitudes.application.port.in.ListarSolicitudesRecibidasDetalladasUseCase;
import com.servify.solicitudes.application.port.in.ListarSolicitudesRecibidasUseCase;
import com.servify.solicitudes.application.port.in.ObtenerEstadoAsignacionSolicitudUseCase;
import com.servify.solicitudes.application.port.in.ObtenerSolicitudServicioUseCase;
import com.servify.solicitudes.application.port.in.ReintentarDistribucionSolicitudUseCase;
import com.servify.solicitudes.application.port.in.ResolverContraofertaUseCase;
import com.servify.solicitudes.application.port.in.ResponderDistribucionSolicitudUseCase;
import com.servify.solicitudes.application.port.out.AsignacionServicioRepositoryPort;
import com.servify.solicitudes.application.port.out.CalificacionRepositoryPort;
import com.servify.solicitudes.application.port.out.ConfiguracionDistribucionPort;
import com.servify.solicitudes.application.port.out.ConfirmacionFinalizacionRepositoryPort;
import com.servify.solicitudes.application.port.out.ContraofertaRepositoryPort;
import com.servify.solicitudes.application.port.out.DistribucionSolicitudRepositoryPort;
import com.servify.solicitudes.application.port.out.PublicacionesCompatiblesPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.application.service.CalificarServicioService;
import com.servify.solicitudes.application.service.ActualizarSolicitudServicioService;
import com.servify.solicitudes.application.service.CancelarSolicitudServicioService;
import com.servify.solicitudes.application.service.ConfirmarAsignacionSolicitudService;
import com.servify.solicitudes.application.service.ConfirmarFinalizacionServicioService;
import com.servify.solicitudes.application.service.ConsultarCalificacionServicioService;
import com.servify.solicitudes.application.service.CrearSolicitudServicioService;
import com.servify.solicitudes.application.service.DistribuidorSolicitudService;
import com.servify.solicitudes.application.service.EmitirContraofertaService;
import com.servify.solicitudes.application.service.ListarSolicitudesDelSolicitanteService;
import com.servify.solicitudes.application.service.ListarSolicitudesRecibidasDetalladasService;
import com.servify.solicitudes.application.service.ListarSolicitudesRecibidasService;
import com.servify.solicitudes.application.service.NotificadorEventosSolicitudService;
import com.servify.solicitudes.application.service.ObtenerEstadoAsignacionSolicitudService;
import com.servify.solicitudes.application.service.ObtenerSolicitudServicioService;
import com.servify.solicitudes.application.service.ReintentarDistribucionSolicitudService;
import com.servify.solicitudes.application.service.ResolverContraofertaService;
import com.servify.solicitudes.application.service.ResponderDistribucionSolicitudService;
import com.servify.solicitudes.domain.service.MotorDistribucionSolicitudes;
import com.servify.solicitudes.domain.service.PoliticaAsignacionUnica;
import com.servify.solicitudes.domain.service.PoliticaCalificacion;
import com.servify.solicitudes.domain.service.PoliticaFinalizacionMutua;
import com.servify.usuarios.application.port.in.ActualizarPerfilUsuarioUseCase;
import com.servify.usuarios.application.port.in.ActualizarCuentaUsuarioUseCase;
import com.servify.usuarios.application.port.in.BuscarPrestadoresPublicosUseCase;
import com.servify.usuarios.application.port.in.CambiarEstadoUsuarioUseCase;
import com.servify.usuarios.application.port.in.CrearUsuarioUseCase;
import com.servify.usuarios.application.port.in.ListarUsuariosUseCase;
import com.servify.usuarios.application.port.in.ObtenerConfiguracionCuentaUseCase;
import com.servify.usuarios.application.port.in.ObtenerPerfilUsuarioUseCase;
import com.servify.usuarios.application.port.in.ObtenerReputacionUsuarioUseCase;
import com.servify.usuarios.application.port.out.PerfilUsuarioRepositoryPort;
import com.servify.usuarios.application.port.out.UsuarioRepositoryPort;
import com.servify.usuarios.application.service.ActualizarPerfilUsuarioService;
import com.servify.usuarios.application.service.ActualizarCuentaUsuarioService;
import com.servify.usuarios.application.service.BuscarPrestadoresPublicosService;
import com.servify.usuarios.application.service.CambiarEstadoUsuarioService;
import com.servify.usuarios.application.service.CrearUsuarioService;
import com.servify.usuarios.application.service.ListarUsuariosService;
import com.servify.usuarios.application.service.ObtenerConfiguracionCuentaService;
import com.servify.usuarios.application.service.ObtenerPerfilUsuarioService;
import com.servify.usuarios.application.service.ObtenerReputacionUsuarioService;
import com.servify.usuarios.domain.service.PoliticaPerfilCompleto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MvpUseCaseConfiguration {

    @Bean
    PoliticaPerfilCompleto politicaPerfilCompleto() {
        return new PoliticaPerfilCompleto();
    }

    @Bean
    ValidadorDisponibilidadHoraria validadorDisponibilidadHoraria() {
        return new ValidadorDisponibilidadHoraria();
    }

    @Bean
    PoliticaCompatibilidadPublicacion politicaCompatibilidadPublicacion() {
        return new PoliticaCompatibilidadPublicacion();
    }

    @Bean
    PoliticaRelevanciaServicio politicaRelevanciaServicio() {
        return new PoliticaRelevanciaServicio();
    }

    @Bean
    MotorDistribucionSolicitudes motorDistribucionSolicitudes() {
        return new MotorDistribucionSolicitudes();
    }

    @Bean
    PoliticaAsignacionUnica politicaAsignacionUnica() {
        return new PoliticaAsignacionUnica();
    }

    @Bean
    PoliticaFinalizacionMutua politicaFinalizacionMutua() {
        return new PoliticaFinalizacionMutua();
    }

    @Bean
    PoliticaCalificacion politicaCalificacion() {
        return new PoliticaCalificacion();
    }

    @Bean
    CrearUsuarioUseCase crearUsuarioUseCase(UsuarioRepositoryPort usuarioRepositoryPort) {
        return new CrearUsuarioService(usuarioRepositoryPort);
    }

    @Bean
    ListarUsuariosUseCase listarUsuariosUseCase(UsuarioRepositoryPort usuarioRepositoryPort) {
        return new ListarUsuariosService(usuarioRepositoryPort);
    }

    @Bean
    ActualizarCuentaUsuarioUseCase actualizarCuentaUsuarioUseCase(UsuarioRepositoryPort usuarioRepositoryPort) {
        return new ActualizarCuentaUsuarioService(usuarioRepositoryPort);
    }

    @Bean
    ActualizarPerfilUsuarioUseCase actualizarPerfilUsuarioUseCase(
            UsuarioRepositoryPort usuarioRepositoryPort,
            PerfilUsuarioRepositoryPort perfilUsuarioRepositoryPort,
            PoliticaPerfilCompleto politicaPerfilCompleto
    ) {
        return new ActualizarPerfilUsuarioService(
                usuarioRepositoryPort,
                perfilUsuarioRepositoryPort,
                politicaPerfilCompleto
        );
    }

    @Bean
    ObtenerPerfilUsuarioUseCase obtenerPerfilUsuarioUseCase(PerfilUsuarioRepositoryPort perfilUsuarioRepositoryPort) {
        return new ObtenerPerfilUsuarioService(perfilUsuarioRepositoryPort);
    }

    @Bean
    ObtenerConfiguracionCuentaUseCase obtenerConfiguracionCuentaUseCase(
            UsuarioRepositoryPort usuarioRepositoryPort,
            PerfilUsuarioRepositoryPort perfilUsuarioRepositoryPort
    ) {
        return new ObtenerConfiguracionCuentaService(usuarioRepositoryPort, perfilUsuarioRepositoryPort);
    }

    @Bean
    ObtenerReputacionUsuarioUseCase obtenerReputacionUsuarioUseCase(
            CalificacionRepositoryPort calificacionRepositoryPort
    ) {
        return new ObtenerReputacionUsuarioService(calificacionRepositoryPort);
    }

    @Bean
    CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase(UsuarioRepositoryPort usuarioRepositoryPort) {
        return new CambiarEstadoUsuarioService(usuarioRepositoryPort);
    }

    @Bean
    BuscarPrestadoresPublicosUseCase buscarPrestadoresPublicosUseCase(
            UsuarioRepositoryPort usuarioRepositoryPort,
            PublicacionServicioRepositoryPort publicacionServicioRepositoryPort,
            CalificacionRepositoryPort calificacionRepositoryPort
    ) {
        return new BuscarPrestadoresPublicosService(
                usuarioRepositoryPort,
                publicacionServicioRepositoryPort,
                calificacionRepositoryPort
        );
    }

    @Bean
    RegistrarCredencialesUseCase registrarCredencialesUseCase(
            CredencialAccesoRepositoryPort credencialAccesoRepositoryPort,
            PasswordHasherPort passwordHasherPort,
            UsuarioAutenticablePort usuarioAutenticablePort
    ) {
        return new RegistrarCredencialesService(
                credencialAccesoRepositoryPort,
                passwordHasherPort,
                usuarioAutenticablePort
        );
    }

    @Bean
    IniciarSesionUseCase iniciarSesionUseCase(
            CredencialAccesoRepositoryPort credencialAccesoRepositoryPort,
            PasswordHasherPort passwordHasherPort,
            TokenProviderPort tokenProviderPort,
            RefreshTokenRepositoryPort refreshTokenRepositoryPort,
            UsuarioAutenticablePort usuarioAutenticablePort
    ) {
        return new IniciarSesionService(
                credencialAccesoRepositoryPort,
                passwordHasherPort,
                tokenProviderPort,
                refreshTokenRepositoryPort,
                usuarioAutenticablePort
        );
    }

    @Bean
    SolicitarRecuperacionPasswordUseCase solicitarRecuperacionPasswordUseCase(
            CredencialAccesoRepositoryPort credencialAccesoRepositoryPort,
            PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort,
            PasswordResetEmailSenderPort passwordResetEmailSenderPort,
            TokenProviderPort tokenProviderPort,
            UsuarioAutenticablePort usuarioAutenticablePort,
            @Value("${servify.password-reset.frontend-url:http://localhost:5173}") String frontendUrl,
            @Value("${servify.password-reset.token-ttl-minutes:30}") long tokenTtlMinutes,
            @Value("${servify.password-reset.expose-debug-token:false}") boolean exposeDebugToken
    ) {
        return new SolicitarRecuperacionPasswordService(
                credencialAccesoRepositoryPort,
                passwordResetTokenRepositoryPort,
                passwordResetEmailSenderPort,
                tokenProviderPort,
                usuarioAutenticablePort,
                frontendUrl,
                tokenTtlMinutes,
                exposeDebugToken
        );
    }

    @Bean
    RestablecerPasswordUseCase restablecerPasswordUseCase(
            PasswordResetTokenRepositoryPort passwordResetTokenRepositoryPort,
            CredencialAccesoRepositoryPort credencialAccesoRepositoryPort,
            PasswordHasherPort passwordHasherPort,
            TokenProviderPort tokenProviderPort
    ) {
        return new RestablecerPasswordService(
                passwordResetTokenRepositoryPort,
                credencialAccesoRepositoryPort,
                passwordHasherPort,
                tokenProviderPort
        );
    }

    @Bean
    RenovarTokenUseCase renovarTokenUseCase(
            RefreshTokenRepositoryPort refreshTokenRepositoryPort,
            CredencialAccesoRepositoryPort credencialAccesoRepositoryPort,
            IdentidadExternaRepositoryPort identidadExternaRepositoryPort,
            TokenProviderPort tokenProviderPort,
            UsuarioAutenticablePort usuarioAutenticablePort
    ) {
        return new RenovarTokenService(
                refreshTokenRepositoryPort,
                credencialAccesoRepositoryPort,
                identidadExternaRepositoryPort,
                tokenProviderPort,
                usuarioAutenticablePort
        );
    }

    @Bean
    AutenticarConIdentidadExternaUseCase autenticarConIdentidadExternaUseCase(
            ProveedorIdentidadVerifierPort proveedorIdentidadVerifierPort,
            IdentidadExternaRepositoryPort identidadExternaRepositoryPort,
            UsuarioRepositoryPort usuarioRepositoryPort,
            PerfilUsuarioRepositoryPort perfilUsuarioRepositoryPort,
            TokenProviderPort tokenProviderPort,
            RefreshTokenRepositoryPort refreshTokenRepositoryPort
    ) {
        return new AutenticarConIdentidadExternaService(
                proveedorIdentidadVerifierPort,
                identidadExternaRepositoryPort,
                usuarioRepositoryPort,
                perfilUsuarioRepositoryPort,
                tokenProviderPort,
                refreshTokenRepositoryPort
        );
    }

    @Bean
    CerrarSesionUseCase cerrarSesionUseCase(
            RefreshTokenRepositoryPort refreshTokenRepositoryPort,
            TokenProviderPort tokenProviderPort
    ) {
        return new CerrarSesionService(refreshTokenRepositoryPort, tokenProviderPort);
    }

    @Bean
    CrearCategoriaServicioUseCase crearCategoriaServicioUseCase(
            CategoriaServicioRepositoryPort categoriaServicioRepositoryPort
    ) {
        return new CrearCategoriaServicioService(categoriaServicioRepositoryPort);
    }

    @Bean
    CambiarEstadoCategoriaServicioUseCase cambiarEstadoCategoriaServicioUseCase(
            CategoriaServicioRepositoryPort categoriaServicioRepositoryPort
    ) {
        return new CambiarEstadoCategoriaServicioService(categoriaServicioRepositoryPort);
    }

    @Bean
    ActualizarCategoriaServicioUseCase actualizarCategoriaServicioUseCase(
            CategoriaServicioRepositoryPort categoriaServicioRepositoryPort
    ) {
        return new ActualizarCategoriaServicioService(categoriaServicioRepositoryPort);
    }

    @Bean
    ListarCategoriasActivasUseCase listarCategoriasActivasUseCase(
            CategoriaServicioRepositoryPort categoriaServicioRepositoryPort
    ) {
        return new ListarCategoriasActivasService(categoriaServicioRepositoryPort);
    }

    @Bean
    ListarCategoriasUseCase listarCategoriasUseCase(
            CategoriaServicioRepositoryPort categoriaServicioRepositoryPort
    ) {
        return new ListarCategoriasService(categoriaServicioRepositoryPort);
    }

    @Bean
    CrearPublicacionUseCase crearPublicacionUseCase(
            PublicacionServicioRepositoryPort publicacionServicioRepositoryPort,
            CategoriaServicioRepositoryPort categoriaServicioRepositoryPort,
            UsuarioPublicadorPort usuarioPublicadorPort,
            ValidadorDisponibilidadHoraria validadorDisponibilidadHoraria
    ) {
        return new CrearPublicacionService(
                publicacionServicioRepositoryPort,
                categoriaServicioRepositoryPort,
                usuarioPublicadorPort,
                validadorDisponibilidadHoraria
        );
    }

    @Bean
    CambiarEstadoPublicacionUseCase cambiarEstadoPublicacionUseCase(
            PublicacionServicioRepositoryPort publicacionServicioRepositoryPort
    ) {
        return new CambiarEstadoPublicacionService(publicacionServicioRepositoryPort);
    }

    @Bean
    ActualizarPublicacionUseCase actualizarPublicacionUseCase(
            PublicacionServicioRepositoryPort publicacionServicioRepositoryPort,
            CategoriaServicioRepositoryPort categoriaServicioRepositoryPort,
            ValidadorDisponibilidadHoraria validadorDisponibilidadHoraria
    ) {
        return new ActualizarPublicacionService(
                publicacionServicioRepositoryPort,
                categoriaServicioRepositoryPort,
                validadorDisponibilidadHoraria
        );
    }

    @Bean
    ObtenerPublicacionUseCase obtenerPublicacionUseCase(
            PublicacionServicioRepositoryPort publicacionServicioRepositoryPort
    ) {
        return new ObtenerPublicacionService(publicacionServicioRepositoryPort);
    }

    @Bean
    ListarPublicacionesDeUsuarioUseCase listarPublicacionesDeUsuarioUseCase(
            PublicacionServicioRepositoryPort publicacionServicioRepositoryPort
    ) {
        return new ListarPublicacionesDeUsuarioService(publicacionServicioRepositoryPort);
    }

    @Bean
    ListarPublicacionesPorCategoriaUseCase listarPublicacionesPorCategoriaUseCase(
            PublicacionServicioRepositoryPort publicacionServicioRepositoryPort
    ) {
        return new ListarPublicacionesPorCategoriaService(publicacionServicioRepositoryPort);
    }

    @Bean
    BuscarPublicacionesCompatiblesUseCase buscarPublicacionesCompatiblesUseCase(
            PublicacionServicioRepositoryPort publicacionServicioRepositoryPort,
            CategoriaServicioRepositoryPort categoriaServicioRepositoryPort,
            PoliticaCompatibilidadPublicacion politicaCompatibilidadPublicacion,
            PoliticaRelevanciaServicio politicaRelevanciaServicio
    ) {
        return new BuscarPublicacionesCompatiblesService(
                publicacionServicioRepositoryPort,
                categoriaServicioRepositoryPort,
                politicaCompatibilidadPublicacion,
                politicaRelevanciaServicio
        );
    }

    @Bean
    DistribuidorSolicitudService distribuidorSolicitudService(
            DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
            PublicacionesCompatiblesPort publicacionesCompatiblesPort,
            ConfiguracionDistribucionPort configuracionDistribucionPort,
            MotorDistribucionSolicitudes motorDistribucionSolicitudes,
            NotificadorEventosSolicitudService notificadorEventosSolicitudService
    ) {
        return new DistribuidorSolicitudService(
                distribucionSolicitudRepositoryPort,
                publicacionesCompatiblesPort,
                configuracionDistribucionPort,
                motorDistribucionSolicitudes,
                notificadorEventosSolicitudService
        );
    }

    @Bean
    CrearSolicitudServicioUseCase crearSolicitudServicioUseCase(
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            DistribuidorSolicitudService distribuidorSolicitudService
    ) {
        return new CrearSolicitudServicioService(
                solicitudServicioRepositoryPort,
                distribuidorSolicitudService
        );
    }

    @Bean
    ObtenerSolicitudServicioUseCase obtenerSolicitudServicioUseCase(
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort
    ) {
        return new ObtenerSolicitudServicioService(solicitudServicioRepositoryPort);
    }

    @Bean
    ActualizarSolicitudServicioUseCase actualizarSolicitudServicioUseCase(
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            DistribuidorSolicitudService distribuidorSolicitudService
    ) {
        return new ActualizarSolicitudServicioService(solicitudServicioRepositoryPort, distribuidorSolicitudService);
    }

    @Bean
    ReintentarDistribucionSolicitudUseCase reintentarDistribucionSolicitudUseCase(
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            DistribuidorSolicitudService distribuidorSolicitudService
    ) {
        return new ReintentarDistribucionSolicitudService(
                solicitudServicioRepositoryPort,
                distribuidorSolicitudService
        );
    }

    @Bean
    ListarSolicitudesDelSolicitanteUseCase listarSolicitudesDelSolicitanteUseCase(
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort
    ) {
        return new ListarSolicitudesDelSolicitanteService(solicitudServicioRepositoryPort);
    }

    @Bean
    ListarSolicitudesRecibidasUseCase listarSolicitudesRecibidasUseCase(
            DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort
    ) {
        return new ListarSolicitudesRecibidasService(
                distribucionSolicitudRepositoryPort,
                solicitudServicioRepositoryPort
        );
    }

    @Bean
    ListarSolicitudesRecibidasDetalladasUseCase listarSolicitudesRecibidasDetalladasUseCase(
            DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort
    ) {
        return new ListarSolicitudesRecibidasDetalladasService(
                distribucionSolicitudRepositoryPort,
                solicitudServicioRepositoryPort
        );
    }

    @Bean
    ResponderDistribucionSolicitudUseCase responderDistribucionSolicitudUseCase(
            DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            NotificadorEventosSolicitudService notificadorEventosSolicitudService
    ) {
        return new ResponderDistribucionSolicitudService(
                distribucionSolicitudRepositoryPort,
                solicitudServicioRepositoryPort,
                notificadorEventosSolicitudService
        );
    }

    @Bean
    EmitirContraofertaUseCase emitirContraofertaUseCase(
            ContraofertaRepositoryPort contraofertaRepositoryPort,
            DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            NotificadorEventosSolicitudService notificadorEventosSolicitudService
    ) {
        return new EmitirContraofertaService(
                contraofertaRepositoryPort,
                distribucionSolicitudRepositoryPort,
                solicitudServicioRepositoryPort,
                notificadorEventosSolicitudService
        );
    }

    @Bean
    ResolverContraofertaUseCase resolverContraofertaUseCase(
            ContraofertaRepositoryPort contraofertaRepositoryPort,
            DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            NotificadorEventosSolicitudService notificadorEventosSolicitudService
    ) {
        return new ResolverContraofertaService(
                contraofertaRepositoryPort,
                distribucionSolicitudRepositoryPort,
                solicitudServicioRepositoryPort,
                notificadorEventosSolicitudService
        );
    }

    @Bean
    ConfirmarAsignacionSolicitudUseCase confirmarAsignacionSolicitudUseCase(
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
            AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
            PoliticaAsignacionUnica politicaAsignacionUnica,
            NotificadorEventosSolicitudService notificadorEventosSolicitudService
    ) {
        return new ConfirmarAsignacionSolicitudService(
                solicitudServicioRepositoryPort,
                distribucionSolicitudRepositoryPort,
                asignacionServicioRepositoryPort,
                politicaAsignacionUnica,
                notificadorEventosSolicitudService
        );
    }

    @Bean
    ConfirmarFinalizacionServicioUseCase confirmarFinalizacionServicioUseCase(
            ConfirmacionFinalizacionRepositoryPort confirmacionFinalizacionRepositoryPort,
            AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            PoliticaFinalizacionMutua politicaFinalizacionMutua,
            NotificadorEventosSolicitudService notificadorEventosSolicitudService
    ) {
        return new ConfirmarFinalizacionServicioService(
                confirmacionFinalizacionRepositoryPort,
                asignacionServicioRepositoryPort,
                solicitudServicioRepositoryPort,
                politicaFinalizacionMutua,
                notificadorEventosSolicitudService
        );
    }

    @Bean
    CalificarServicioUseCase calificarServicioUseCase(
            CalificacionRepositoryPort calificacionRepositoryPort,
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
            PoliticaCalificacion politicaCalificacion,
            NotificadorEventosSolicitudService notificadorEventosSolicitudService
    ) {
        return new CalificarServicioService(
                calificacionRepositoryPort,
                solicitudServicioRepositoryPort,
                asignacionServicioRepositoryPort,
                politicaCalificacion,
                notificadorEventosSolicitudService
        );
    }

    @Bean
    ConsultarCalificacionServicioUseCase consultarCalificacionServicioUseCase(
            CalificacionRepositoryPort calificacionRepositoryPort
    ) {
        return new ConsultarCalificacionServicioService(calificacionRepositoryPort);
    }

    @Bean
    CancelarSolicitudServicioUseCase cancelarSolicitudServicioUseCase(
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
            NotificadorEventosSolicitudService notificadorEventosSolicitudService
    ) {
        return new CancelarSolicitudServicioService(
                solicitudServicioRepositoryPort,
                distribucionSolicitudRepositoryPort,
                notificadorEventosSolicitudService
        );
    }

    @Bean
    ObtenerEstadoAsignacionSolicitudUseCase obtenerEstadoAsignacionSolicitudUseCase(
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
            AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
            ContraofertaRepositoryPort contraofertaRepositoryPort,
            ConfirmacionFinalizacionRepositoryPort confirmacionFinalizacionRepositoryPort
    ) {
        return new ObtenerEstadoAsignacionSolicitudService(
                solicitudServicioRepositoryPort,
                distribucionSolicitudRepositoryPort,
                asignacionServicioRepositoryPort,
                contraofertaRepositoryPort,
                confirmacionFinalizacionRepositoryPort
        );
    }

    @Bean
    ModerarPublicacionUseCase moderarPublicacionUseCase(
            PublicacionModerablePort publicacionModerablePort,
            UsuarioAdministrablePort usuarioAdministrablePort
    ) {
        return new ModerarPublicacionService(publicacionModerablePort, usuarioAdministrablePort);
    }

    @Bean
    ListarPublicacionesAdminUseCase listarPublicacionesAdminUseCase(
            PublicacionServicioRepositoryPort publicacionServicioRepositoryPort
    ) {
        return new ListarPublicacionesAdminService(publicacionServicioRepositoryPort);
    }

    @Bean
    ObtenerConfiguracionGeneralUseCase obtenerConfiguracionGeneralUseCase(
            ConfiguracionGeneralRepositoryPort configuracionGeneralRepositoryPort
    ) {
        return new ObtenerConfiguracionGeneralService(configuracionGeneralRepositoryPort);
    }

    @Bean
    ActualizarConfiguracionGeneralUseCase actualizarConfiguracionGeneralUseCase(
            ConfiguracionGeneralRepositoryPort configuracionGeneralRepositoryPort,
            UsuarioAdministrablePort usuarioAdministrablePort
    ) {
        return new ActualizarConfiguracionGeneralService(
                configuracionGeneralRepositoryPort,
                usuarioAdministrablePort
        );
    }

    @Bean
    AplicarMedidaAdministrativaUsuarioUseCase aplicarMedidaAdministrativaUsuarioUseCase(
            MedidaAdministrativaUsuarioRepositoryPort medidaAdministrativaUsuarioRepositoryPort,
            UsuarioAdministrablePort usuarioAdministrablePort
    ) {
        return new AplicarMedidaAdministrativaUsuarioService(
                medidaAdministrativaUsuarioRepositoryPort,
                usuarioAdministrablePort
        );
    }

    @Bean
    ObtenerMedidasAdministrativasDeUsuarioUseCase obtenerMedidasAdministrativasDeUsuarioUseCase(
            MedidaAdministrativaUsuarioRepositoryPort medidaAdministrativaUsuarioRepositoryPort
    ) {
        return new ObtenerMedidasAdministrativasDeUsuarioService(medidaAdministrativaUsuarioRepositoryPort);
    }

    @Bean
    CrearNotificacionUsuarioUseCase crearNotificacionUsuarioUseCase(
            NotificacionUsuarioRepositoryPort notificacionUsuarioRepositoryPort
    ) {
        return new CrearNotificacionUsuarioService(notificacionUsuarioRepositoryPort);
    }

    @Bean
    NotificadorEventosSolicitudService notificadorEventosSolicitudService(
            CrearNotificacionUsuarioUseCase crearNotificacionUsuarioUseCase,
            UsuarioRepositoryPort usuarioRepositoryPort,
            PerfilUsuarioRepositoryPort perfilUsuarioRepositoryPort
    ) {
        return new NotificadorEventosSolicitudService(
                crearNotificacionUsuarioUseCase,
                usuarioRepositoryPort,
                perfilUsuarioRepositoryPort
        );
    }

    @Bean
    ListarNotificacionesUsuarioUseCase listarNotificacionesUsuarioUseCase(
            NotificacionUsuarioRepositoryPort notificacionUsuarioRepositoryPort
    ) {
        return new ListarNotificacionesUsuarioService(notificacionUsuarioRepositoryPort);
    }

    @Bean
    MarcarNotificacionLeidaUseCase marcarNotificacionLeidaUseCase(
            NotificacionUsuarioRepositoryPort notificacionUsuarioRepositoryPort
    ) {
        return new MarcarNotificacionLeidaService(notificacionUsuarioRepositoryPort);
    }

    @Bean
    EliminarNotificacionUsuarioUseCase eliminarNotificacionUsuarioUseCase(
            NotificacionUsuarioRepositoryPort notificacionUsuarioRepositoryPort
    ) {
        return new EliminarNotificacionUsuarioService(notificacionUsuarioRepositoryPort);
    }

    @Bean
    ChatSolicitudService chatSolicitudService(
            MensajeChatRepositoryPort mensajeChatRepositoryPort,
            SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
            DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
            AsignacionServicioRepositoryPort asignacionServicioRepositoryPort,
            CrearNotificacionUsuarioUseCase crearNotificacionUsuarioUseCase
    ) {
        return new ChatSolicitudService(
                mensajeChatRepositoryPort,
                solicitudServicioRepositoryPort,
                distribucionSolicitudRepositoryPort,
                asignacionServicioRepositoryPort,
                crearNotificacionUsuarioUseCase
        );
    }

}
