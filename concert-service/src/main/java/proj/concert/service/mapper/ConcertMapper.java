package proj.concert.service.mapper;

import proj.concert.common.dto.PerformerDTO;
import proj.concert.service.domain.Concert;
import proj.concert.common.dto.ConcertDTO;
import proj.concert.service.domain.Performer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConcertMapper {

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
