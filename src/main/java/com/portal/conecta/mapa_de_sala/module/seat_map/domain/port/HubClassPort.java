import com.portal.conecta.mapa_de_sala.module.seat_map.domain.model.hub.HubStudent;

import java.util.List;
import java.util.UUID;

/**
 * Porta de integração com o Hub para consulta da turma própria de um usuário.
 */
public interface HubClassPort {

    UUID getClassIdForUser(UUID userId);

    boolean existsById(UUID classId);

    List<HubStudent> findStudentsByClassId(UUID classId);
}
