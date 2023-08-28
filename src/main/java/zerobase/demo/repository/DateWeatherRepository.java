package zerobase.demo.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.demo.domain.DateWeather;

@Repository
public interface DateWeatherRepository extends JpaRepository<DateWeather, Long> {

    List<DateWeather> findAllByDate(LocalDate date);
}
