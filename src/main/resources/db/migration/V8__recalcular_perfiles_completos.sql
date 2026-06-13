UPDATE public.perfil_usuario
SET perfil_completo = true,
    updated_at = now()
WHERE perfil_completo = false
  AND nombre IS NOT NULL
  AND btrim(nombre) <> ''
  AND apellido IS NOT NULL
  AND btrim(apellido) <> ''
  AND edad BETWEEN 18 AND 120
  AND (
      (latitud BETWEEN -90 AND 90 AND longitud BETWEEN -180 AND 180)
      OR (
          localidad IS NOT NULL
          AND btrim(localidad) <> ''
          AND ciudad IS NOT NULL
          AND btrim(ciudad) <> ''
          AND provincia IS NOT NULL
          AND btrim(provincia) <> ''
      )
  );
