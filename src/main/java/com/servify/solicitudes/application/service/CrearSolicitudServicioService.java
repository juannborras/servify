package com.servify.solicitudes.application.service;

import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.valueobject.Ubicacion;
import com.servify.solicitudes.application.dto.CrearSolicitudServicioCommand;
import com.servify.solicitudes.application.dto.DisponibilidadHorariaResult;
import com.servify.solicitudes.application.dto.SolicitudServicioResult;
import com.servify.solicitudes.application.dto.UbicacionSolicitudResult;
import com.servify.solicitudes.application.port.in.CrearSolicitudServicioUseCase;
import com.servify.solicitudes.application.port.out.ServicioRecurrenciaRepositoryPort;
import com.servify.solicitudes.application.port.out.SolicitudServicioRepositoryPort;
import com.servify.solicitudes.domain.enumtype.EstadoRecurrenciaServicio;
import com.servify.solicitudes.domain.enumtype.EstadoSolicitud;
import com.servify.solicitudes.domain.enumtype.FrecuenciaRecurrencia;
import com.servify.solicitudes.domain.enumtype.TipoProgramacionSolicitud;
import com.servify.solicitudes.domain.model.ServicioRecurrencia;
import com.servify.solicitudes.domain.model.SolicitudServicio;
import com.servify.solicitudes.application.port.out.ConfiguracionDistribucionPort;
import com.servify.solicitudes.application.port.out.DistribucionSolicitudRepositoryPort;
import com.servify.solicitudes.application.port.out.PublicacionesCompatiblesPort;
import com.servify.solicitudes.domain.service.MotorDistribucionSolicitudes;

import java.time.LocalDateTime;
import java.util.UUID;

public class CrearSolicitudServicioService implements CrearSolicitudServicioUseCase {

    private final SolicitudServicioRepositoryPort solicitudServicioRepositoryPort;
    private final DistribuidorSolicitudService distribuidorSolicitudService;
    private final ServicioRecurrenciaRepositoryPort servicioRecurrenciaRepositoryPort;

    public CrearSolicitudServicioService(SolicitudServicioRepositoryPort solicitudServicioRepositoryPort) {
        this(solicitudServicioRepositoryPort, null, null);
    }

    public CrearSolicitudServicioService(SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
                                         DistribucionSolicitudRepositoryPort distribucionSolicitudRepositoryPort,
                                         PublicacionesCompatiblesPort publicacionesCompatiblesPort,
                                         ConfiguracionDistribucionPort configuracionDistribucionPort,
                                         MotorDistribucionSolicitudes motorDistribucionSolicitudes) {
        this(solicitudServicioRepositoryPort, new DistribuidorSolicitudService(
                distribucionSolicitudRepositoryPort,
                publicacionesCompatiblesPort,
                configuracionDistribucionPort,
                motorDistribucionSolicitudes
        ));
    }

    public CrearSolicitudServicioService(SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
                                         DistribuidorSolicitudService distribuidorSolicitudService) {
        this(solicitudServicioRepositoryPort, distribuidorSolicitudService, null);
    }

    public CrearSolicitudServicioService(SolicitudServicioRepositoryPort solicitudServicioRepositoryPort,
                                         DistribuidorSolicitudService distribuidorSolicitudService,
                                         ServicioRecurrenciaRepositoryPort servicioRecurrenciaRepositoryPort) {
        this.solicitudServicioRepositoryPort = solicitudServicioRepositoryPort;
        this.distribuidorSolicitudService = distribuidorSolicitudService;
        this.servicioRecurrenciaRepositoryPort = servicioRecurrenciaRepositoryPort;
    }

    @Override
    public SolicitudServicioResult crear(CrearSolicitudServicioCommand command) {
        // Crea una nueva `SolicitudServicio` a partir del comando.
        // - Valida los campos obligatorios y las invariantes del VO (ubicación, disponibilidad).
        // - Persiste la entidad mediante el repositorio y devuelve el DTO resultante.
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo");
        }
        if (command.getSolicitanteId() == null) {
            throw new IllegalArgumentException("solicitanteId no puede ser nulo");
        }
        if (command.getCategoriaServicioId() == null) {
            throw new IllegalArgumentException("categoriaServicioId no puede ser nulo");
        }
        if (command.getModalidadServicio() == null) {
            throw new IllegalArgumentException("modalidadServicio no puede ser nula");
        }
        if (command.getUbicacion() == null) {
            throw new IllegalArgumentException("ubicacion no puede ser nula");
        }
        if (!command.getUbicacion().esAptaParaBusquedaGeografica()) {
            throw new IllegalArgumentException("La ubicación no es apta para búsqueda geográfica");
        }
        if (command.getDisponibilidadRequerida() == null) {
            throw new IllegalArgumentException("disponibilidadRequerida no puede ser nula");
        }
        if (!command.getDisponibilidadRequerida().esRangoHorarioValido()) {
            throw new IllegalArgumentException("La disponibilidad horaria no es válida");
        }

        validarProgramacion(command);
        SolicitudServicio solicitud = construirSolicitud(command);
        SolicitudServicio persistida = this.solicitudServicioRepositoryPort.guardar(solicitud);
        guardarRecurrenciaSiCorresponde(command, persistida);
        distribuirSolicitudSiCorresponde(persistida);
        return construirResultado(persistida);
    }

    protected void distribuirSolicitudSiCorresponde(SolicitudServicio solicitud) {
        if (distribuidorSolicitudService != null) {
            distribuidorSolicitudService.distribuir(solicitud);
        }
    }

    protected SolicitudServicio construirSolicitud(CrearSolicitudServicioCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("El comando no puede ser nulo");
        }
        return new SolicitudServicio(
                generarIdSolicitud(),
                command.getSolicitanteId(),
                command.getCategoriaServicioId(),
                command.getModalidadServicio(),
                command.getUbicacion(),
                command.getDisponibilidadRequerida(),
                command.getDescripcionNecesidad(),
                command.getPrecioReferencia(),
                obtenerEstadoInicial(),
                obtenerFechaActual(),
                tipoProgramacion(command),
                command.getFechaProgramadaInicio(),
                command.getFechaProgramadaFin()
        );
    }

    protected SolicitudServicioResult construirResultado(SolicitudServicio solicitudServicio) {
        if (solicitudServicio == null) {
            return null;
        }
        return new SolicitudServicioResult(
                solicitudServicio.getId(),
                solicitudServicio.getSolicitanteId(),
                solicitudServicio.getCategoriaServicioId(),
                solicitudServicio.getModalidadServicio(),
                construirUbicacionResult(solicitudServicio.getUbicacion()),
                construirDisponibilidadResult(solicitudServicio.getDisponibilidadRequerida()),
                solicitudServicio.getDescripcionNecesidad(),
                solicitudServicio.getPrecioReferencia(),
                solicitudServicio.getEstado(),
                solicitudServicio.getFechaSolicitud(),
                solicitudServicio.getTipoProgramacion(),
                solicitudServicio.getFechaProgramadaInicio(),
                solicitudServicio.getFechaProgramadaFin()
        );
    }

    protected UbicacionSolicitudResult construirUbicacionResult(Ubicacion ubicacion) {
        if (ubicacion == null) {
            return null;
        }
        return new UbicacionSolicitudResult(
                ubicacion.getPais(),
                ubicacion.getProvincia(),
                ubicacion.getCiudad(),
                ubicacion.getLocalidad(),
                ubicacion.getCalle(),
                ubicacion.getAltura(),
                ubicacion.getReferencia(),
                ubicacion.getLatitud(),
                ubicacion.getLongitud()
        );
    }

    protected DisponibilidadHorariaResult construirDisponibilidadResult(DisponibilidadHoraria disponibilidadHoraria) {
        if (disponibilidadHoraria == null) {
            return null;
        }
        return new DisponibilidadHorariaResult(
                disponibilidadHoraria.getDiaSemana(),
                disponibilidadHoraria.getHoraDesde(),
                disponibilidadHoraria.getHoraHasta()
        );
    }

    protected EstadoSolicitud obtenerEstadoInicial() {
        return EstadoSolicitud.BUSCANDO_PRESTADOR;
    }

    protected UUID generarIdSolicitud() {
        return UUID.randomUUID();
    }

    protected LocalDateTime obtenerFechaActual() {
        return LocalDateTime.now();
    }

    private void validarProgramacion(CrearSolicitudServicioCommand command) {
        TipoProgramacionSolicitud tipo = tipoProgramacion(command);
        if (tipo == TipoProgramacionSolicitud.PROGRAMADA) {
            validarFechasProgramadas(command.getFechaProgramadaInicio(), command.getFechaProgramadaFin());
        }
        if (tipo == TipoProgramacionSolicitud.RECURRENTE) {
            validarRecurrencia(command);
        }
        if (tipo == TipoProgramacionSolicitud.INMEDIATA
                && command.getFechaProgramadaInicio() != null
                && command.getFechaProgramadaFin() != null) {
            validarFechasProgramadas(command.getFechaProgramadaInicio(), command.getFechaProgramadaFin());
        }
    }

    private TipoProgramacionSolicitud tipoProgramacion(CrearSolicitudServicioCommand command) {
        if (command.getTipoProgramacion() != null) {
            return command.getTipoProgramacion();
        }
        if (command.getFrecuenciaRecurrencia() != null) {
            return TipoProgramacionSolicitud.RECURRENTE;
        }
        if (command.getFechaProgramadaInicio() != null || command.getFechaProgramadaFin() != null) {
            return TipoProgramacionSolicitud.PROGRAMADA;
        }
        return TipoProgramacionSolicitud.INMEDIATA;
    }

    private void validarFechasProgramadas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las solicitudes programadas requieren fecha de inicio y fin");
        }
        if (!fechaInicio.isBefore(fechaFin)) {
            throw new IllegalArgumentException("La fecha programada de inicio debe ser anterior a la fecha de fin");
        }
        if (fechaInicio.isBefore(obtenerFechaActual().minusMinutes(5))) {
            throw new IllegalArgumentException("No se puede programar una solicitud en el pasado");
        }
    }

    private void validarRecurrencia(CrearSolicitudServicioCommand command) {
        if (command.getFrecuenciaRecurrencia() == null) {
            throw new IllegalArgumentException("frecuenciaRecurrencia no puede ser nula para solicitudes recurrentes");
        }
        if (command.getFechaInicioRecurrencia() == null) {
            throw new IllegalArgumentException("fechaInicioRecurrencia no puede ser nula para solicitudes recurrentes");
        }
        if (command.getFechaFinRecurrencia() != null
                && command.getFechaFinRecurrencia().isBefore(command.getFechaInicioRecurrencia())) {
            throw new IllegalArgumentException("fechaFinRecurrencia no puede ser anterior a fechaInicioRecurrencia");
        }
        if (servicioRecurrenciaRepositoryPort == null) {
            throw new IllegalStateException("La persistencia de recurrencias no esta configurada");
        }
    }

    private void guardarRecurrenciaSiCorresponde(CrearSolicitudServicioCommand command, SolicitudServicio solicitud) {
        if (tipoProgramacion(command) != TipoProgramacionSolicitud.RECURRENTE) {
            return;
        }
        DisponibilidadHoraria disponibilidad = command.getDisponibilidadRequerida();
        FrecuenciaRecurrencia frecuencia = command.getFrecuenciaRecurrencia();
        ServicioRecurrencia recurrencia = new ServicioRecurrencia(
                UUID.randomUUID(),
                solicitud.getId(),
                null,
                frecuencia,
                disponibilidad.getDiaSemana(),
                disponibilidad.getHoraDesde(),
                disponibilidad.getHoraHasta(),
                command.getFechaInicioRecurrencia(),
                command.getFechaFinRecurrencia(),
                EstadoRecurrenciaServicio.BUSCANDO_PRESTADOR,
                null,
                null,
                null
        );
        servicioRecurrenciaRepositoryPort.guardar(recurrencia);
    }
}
