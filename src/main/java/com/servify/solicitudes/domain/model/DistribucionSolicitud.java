package com.servify.solicitudes.domain.model;

import com.servify.shared.domain.model.BaseEntity;
import com.servify.solicitudes.domain.enumtype.EstadoDistribucion;

import java.time.LocalDateTime;
import java.util.UUID;

//Metí:
//solicitudId
//publicacionServicioId
//prestadorId
//porque cada distribución está asociada a:
//una solicitud
//una publicación compatible
//el prestador dueño de esa publicación
//También agregué:
//rondaDistribucion para modelar la distribución progresiva
//fechaEnvio
//fechaRespuesta
//fechaExpiracion
//
//Eso les va a servir bastante después para trazabilidad, matching y reglas de expiración.

public class DistribucionSolicitud extends BaseEntity {

    private UUID solicitudId;
    private UUID publicacionServicioId;
    private UUID prestadorId;
    private EstadoDistribucion estado;
    private Integer rondaDistribucion;
    private LocalDateTime fechaEnvio;
    private LocalDateTime fechaRespuesta;
    private LocalDateTime fechaExpiracion;

    protected DistribucionSolicitud() {
    }

    public DistribucionSolicitud(UUID id,
                                 UUID solicitudId,
                                 UUID publicacionServicioId,
                                 UUID prestadorId,
                                 EstadoDistribucion estado,
                                 Integer rondaDistribucion,
                                 LocalDateTime fechaEnvio,
                                 LocalDateTime fechaRespuesta,
                                 LocalDateTime fechaExpiracion) {
        super(id);
        this.solicitudId = solicitudId;
        this.publicacionServicioId = publicacionServicioId;
        this.prestadorId = prestadorId;
        this.estado = estado;
        this.rondaDistribucion = rondaDistribucion;
        this.fechaEnvio = fechaEnvio;
        this.fechaRespuesta = fechaRespuesta;
        this.fechaExpiracion = fechaExpiracion;
    }

    public UUID getSolicitudId() {
        return solicitudId;
    }

    public UUID getPublicacionServicioId() {
        return publicacionServicioId;
    }

    public UUID getPrestadorId() {
        return prestadorId;
    }

    public EstadoDistribucion getEstado() {
        return estado;
    }

    public Integer getRondaDistribucion() {
        return rondaDistribucion;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public boolean estaEnviada() {
        return this.estado == EstadoDistribucion.ENVIADA;
    }

    public boolean estaAceptada() {
        return this.estado == EstadoDistribucion.ACEPTADA;
    }

    public boolean estaRechazada() {
        return this.estado == EstadoDistribucion.RECHAZADA;
    }

    public boolean estaContraofertada() {
        return this.estado == EstadoDistribucion.CONTRAOFERTADA;
    }

    public boolean estaExpirada() {
        return this.estado == EstadoDistribucion.EXPIRADA;
    }

    public boolean estaCerrada() {
        return this.estado == EstadoDistribucion.CERRADA;
    }

    public boolean fueRespondida() {
        return this.estado == EstadoDistribucion.ACEPTADA || 
               this.estado == EstadoDistribucion.RECHAZADA ||
               this.estado == EstadoDistribucion.CONTRAOFERTADA ||
               this.estado == EstadoDistribucion.EXPIRADA ||
               this.estado == EstadoDistribucion.CERRADA;
    }

    public boolean puedeSerRespondida() {
        return this.estado == EstadoDistribucion.ENVIADA;
    }

    public boolean perteneceAPrestador(UUID prestadorId) {
        if (prestadorId == null) {
            return false;
        }
        return this.prestadorId.equals(prestadorId);
    }

    public void marcarComoEnviada() {
        this.estado = EstadoDistribucion.ENVIADA;
    }

    public void reasignarPublicacion(UUID publicacionServicioId) {
        if (publicacionServicioId == null) {
            throw new IllegalArgumentException("publicacionServicioId no puede ser nulo");
        }
        if (!estaEnviada()) {
            throw new IllegalStateException("Solo se puede reasignar una distribucion pendiente");
        }
        this.publicacionServicioId = publicacionServicioId;
    }

    public void aceptar(LocalDateTime fechaRespuesta) {
        if (fechaRespuesta == null) {
            throw new IllegalArgumentException("La fecha de respuesta no puede ser nula");
        }
        if (!puedeSerRespondida()) {
            throw new IllegalArgumentException("La distribución no puede ser respondida en su estado actual");
        }
        this.estado = EstadoDistribucion.ACEPTADA;
        this.fechaRespuesta = fechaRespuesta;
    }

    public void rechazar(LocalDateTime fechaRespuesta) {
        if (fechaRespuesta == null) {
            throw new IllegalArgumentException("La fecha de respuesta no puede ser nula");
        }
        if (!puedeSerRespondida()) {
            throw new IllegalArgumentException("La distribución no puede ser respondida en su estado actual");
        }
        this.estado = EstadoDistribucion.RECHAZADA;
        this.fechaRespuesta = fechaRespuesta;
    }

    public void marcarComoContraofertada(LocalDateTime fechaRespuesta) {
        if (fechaRespuesta == null) {
            throw new IllegalArgumentException("La fecha de respuesta no puede ser nula");
        }
        if (!puedeSerRespondida()) {
            throw new IllegalArgumentException("La distribución no puede ser respondida en su estado actual");
        }
        this.estado = EstadoDistribucion.CONTRAOFERTADA;
        this.fechaRespuesta = fechaRespuesta;
    }

    public void aceptarContraoferta(LocalDateTime fechaRespuesta) {
        if (fechaRespuesta == null) {
            throw new IllegalArgumentException("La fecha de respuesta no puede ser nula");
        }
        if (!estaContraofertada()) {
            throw new IllegalArgumentException("La distribucion no tiene una contraoferta pendiente de resolucion");
        }
        this.estado = EstadoDistribucion.ACEPTADA;
        this.fechaRespuesta = fechaRespuesta;
    }

    public void rechazarContraoferta(LocalDateTime fechaRespuesta) {
        if (fechaRespuesta == null) {
            throw new IllegalArgumentException("La fecha de respuesta no puede ser nula");
        }
        if (!estaContraofertada()) {
            throw new IllegalArgumentException("La distribucion no tiene una contraoferta pendiente de resolucion");
        }
        this.estado = EstadoDistribucion.RECHAZADA;
        this.fechaRespuesta = fechaRespuesta;
    }

    public void expirar(LocalDateTime fechaExpiracion) {
        if (this.estaEnviada()) {
            this.estado = EstadoDistribucion.EXPIRADA;
        }
    }

    public void cerrar() {
        this.estado = EstadoDistribucion.CERRADA;
    }
}
