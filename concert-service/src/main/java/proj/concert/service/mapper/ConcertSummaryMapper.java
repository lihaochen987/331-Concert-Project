package proj.concert.service.mapper;

import proj.concert.common.dto.ConcertDTO;
import proj.concert.common.dto.ConcertSummaryDTO;
import proj.concert.common.dto.PerformerDTO;
import proj.concert.service.domain.Concert;
import proj.concert.service.domain.ConcertSummary;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hibernate.tool.schema.SchemaToolingLogging.LOGGER;

public class ConcertSummaryMapper {

    public static ConcertSummaryDTO toDto(ConcertSummary concertSummary) {
        return new ConcertSummaryDTO(concertSummary.getId(), concertSummary.getTitle(), concertSummary.getImageName());
    }
}
