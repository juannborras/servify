package com.servify.usuarios.infrastructure.web;

import com.servify.usuarios.application.dto.PrestadorPublicoResult;
import com.servify.usuarios.application.port.in.BuscarPrestadoresPublicosUseCase;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/prestadores")
public class PrestadoresApiController {

    private final BuscarPrestadoresPublicosUseCase buscarPrestadoresPublicosUseCase;

    public PrestadoresApiController(BuscarPrestadoresPublicosUseCase buscarPrestadoresPublicosUseCase) {
        this.buscarPrestadoresPublicosUseCase = buscarPrestadoresPublicosUseCase;
    }

    @GetMapping
    public ResponseEntity<List<PrestadorPublicoResult>> buscarPrestadores(
            @RequestParam(required = false) String nombreUsuario
    ) {
        return ResponseEntity.ok(buscarPrestadoresPublicosUseCase.buscarPorNombreUsuario(nombreUsuario));
    }

    @GetMapping("/{usuarioId}")
    public ResponseEntity<PrestadorPublicoResult> obtenerPrestador(@PathVariable UUID usuarioId) {
        return buscarPrestadoresPublicosUseCase.obtenerPorUsuarioId(usuarioId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
