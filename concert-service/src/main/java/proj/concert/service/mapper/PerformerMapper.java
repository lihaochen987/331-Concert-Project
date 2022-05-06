package proj.concert.service.mapper;

import proj.concert.common.dto.PerformerDTO;
import proj.concert.service.domain.Performer;

import java.util.ArrayList;
import java.util.List;

public class PerformerMapper {
    public static PerformerDTO toDto(Performer performer) {
        return new PerformerDTO(performer.getId(), performer.getName(), performer.getImageName(), performer.getGenre(), performer.getBlurb());
    }
}
