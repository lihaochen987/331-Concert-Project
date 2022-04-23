package proj.concert.service.mapper;

import proj.concert.common.dto.PerformerDTO;
import proj.concert.service.domain.Performer;

import java.util.ArrayList;
import java.util.List;

public class PerformerMapper {
    //TODO Implement later
//    public static List<Performer> toDomainModel(List<Performer> dtoPerformer) {
//        return new ArrayList<>();
//    }

    public static PerformerDTO toDto(Performer performer) {
        return new PerformerDTO(performer.getId(), performer.getName(), performer.getImageName(), performer.getGenre(), performer.getBlurb());
    }
}
