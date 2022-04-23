package proj.concert.service.mapper;

import proj.concert.common.dto.PerformerDTO;
import proj.concert.service.domain.Concert;
import proj.concert.service.domain.Performer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PerformerMapper {
    public static List<Performer> toDomainModel(List<Performer> dtoPerformer) {
        return new ArrayList<>();
        //TODO Fix implementation
    }

    public static List<PerformerDTO> toDto(List<Performer> performer) {
        List<PerformerDTO> performerList = new ArrayList<>();

        for (Performer p: performer){
            PerformerDTO dtoPerformer = new PerformerDTO(p.getId(), p.getName(), p.getImageName(), p.getGenre(), p.getBlurb());
            performerList.add(dtoPerformer);
        }

        return performerList;
    }
}
