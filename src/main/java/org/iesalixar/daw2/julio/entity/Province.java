package org.iesalixar.daw2.julio.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

 @Data
 @NoArgsConstructor
 @AllArgsConstructor
public class Province {
     private int id;
     private  String code;
     private String name;
     private int id_region;
     /**
      * Este es un constructor personalizado que no incluye el campo `id`.
      * Se utiliza para crear instancias de `Province` cuando no es necesario o no
      se conoce el `id` de la province
      * (por ejemplo, antes de insertar la province en la base de datos, donde el
      `id` es autogenerado).
      * @param code Código de la province.
      * @param name Nombre de la province.
      * @param id_region de la province
      */

     public Province(String code, String name, int id_region){
         this.code=code;
         this.name=name;
         this.id_region=id_region;
     }
}
