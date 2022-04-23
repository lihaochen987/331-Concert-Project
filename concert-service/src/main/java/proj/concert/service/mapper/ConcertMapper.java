package proj.concert.service.mapper;

import proj.concert.common.dto.PerformerDTO;
import proj.concert.service.domain.Concert;
import proj.concert.common.dto.ConcertDTO;
import proj.concert.service.domain.Performer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.hibernate.tool.schema.SchemaToolingLogging.LOGGER;

public class ConcertMapper {

    public static Concert toDomainModel(ConcertDTO dtoConcert) {
        return new Concert(dtoConcert.getId(), dtoConcert.getTitle(), dtoConcert.getImageName(), dtoConcert.getBlurb());
        //TODO Fix implementation
    }

    public static ConcertDTO toDto(Concert concert) {
        List<Performer> performers = concert.getPerformers();
        List<PerformerDTO> performerDTOList = new ArrayList<PerformerDTO>();

        for (Performer performer : performers){
            PerformerDTO dtoPerformer = PerformerMapper.toDto(performer);
            performerDTOList.add(dtoPerformer);
        }

        List<LocalDateTime> dates = new ArrayList<>(concert.getDates());

        ConcertDTO dtoConcert = new ConcertDTO(concert.getId(), concert.getTitle(), concert.getImageName(), concert.getBlurb());
        dtoConcert.setPerformers(performerDTOList);
        dtoConcert.setDates(dates);

        return dtoConcert;
    }
}
