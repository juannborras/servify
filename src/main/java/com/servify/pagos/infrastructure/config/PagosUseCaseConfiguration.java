package com.servify.pagos.infrastructure.config;

import com.servify.pagos.application.port.in.ConsultarPagoServicioUseCase;
import com.servify.pagos.application.port.in.IniciarPagoServicioUseCase;
import com.servify.pagos.application.port.in.SincronizarPagoServicioUseCase;
import com.servify.pagos.application.port.out.EstadoIntegracionPagoPort;
import com.servify.pagos.application.port.out.MercadoPagoGatewayPort;
import com.servify.pagos.application.port.out.PagoServicioRepositoryPort;
import com.servify.pagos.application.service.ConsultarPagoServicioService;
import com.servify.pagos.application.service.IniciarPagoServicioService;
import com.servify.pagos.application.service.SincronizarPagoServicioService;
import com.servify.solicitudes.application.port.in.ConfirmarFinalizacionServicioUseCase;
import com.servify.solicitudes.application.port.out.AsignacionServicioRepositoryPort;
import com.servify.solicitudes.application.port.out.ConfirmacionFinalizacionRepositoryPort;
import com.servify.solicitudes.application.port.out.ServicioEncuentroRepositoryPort;
import com.servify.solicitudes.application.port.out.ServicioRecurrenciaRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PagosUseCaseConfiguration {

    @Bean
    IniciarPagoServicioUseCase iniciarPagoServicioUseCase(
            PagoServicioRepositoryPort pagoRepository,
            MercadoPagoGatewayPort mercadoPagoGateway,
            EstadoIntegracionPagoPort estadoIntegracion,
            SolicitudServicioRepositoryPort solicitudRepository,
            AsignacionServicioRepositoryPort asignacionRepository,
            ServicioEncuentroRepositoryPort encuentroRepository,
            ServicioRecurrenciaRepositoryPort recurrenciaRepository) {
        return new IniciarPagoServicioService(pagoRepository, mercadoPagoGateway, estadoIntegracion,
                solicitudRepository, asignacionRepository, encuentroRepository, recurrenciaRepository);
    }

    @Bean
    SincronizarPagoServicioUseCase sincronizarPagoServicioUseCase(
            PagoServicioRepositoryPort pagoRepository,
            MercadoPagoGatewayPort mercadoPagoGateway,
            EstadoIntegracionPagoPort estadoIntegracion,
            ConfirmarFinalizacionServicioUseCase confirmarFinalizacionUseCase,
            ConfirmacionFinalizacionRepositoryPort confirmacionRepository) {
        return new SincronizarPagoServicioService(pagoRepository, mercadoPagoGateway, estadoIntegracion,
                confirmarFinalizacionUseCase, confirmacionRepository);
    }

    @Bean
    ConsultarPagoServicioUseCase consultarPagoServicioUseCase(
            PagoServicioRepositoryPort pagoRepository,
            SolicitudServicioRepositoryPort solicitudRepository,
            AsignacionServicioRepositoryPort asignacionRepository) {
        return new ConsultarPagoServicioService(pagoRepository, solicitudRepository, asignacionRepository);
    }
}
