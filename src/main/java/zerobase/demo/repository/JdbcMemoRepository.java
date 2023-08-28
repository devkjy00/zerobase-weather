package zerobase.demo.repository;

import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import zerobase.demo.domain.Memo;

@Repository
public class JdbcMemoRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcMemoRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Memo save(Memo memo) {
        String sql = "INSERT INTO memo(text) VALUES (?)";
        jdbcTemplate.update(sql, memo.getText());
        return memo;
    }

    public List<Memo> findAll(){
        String sql = "SELECT * FROM memo";
        return jdbcTemplate.query(sql, memoRowMapper());
    }

    public Optional<Memo> findById(int id){
        String sql = "SELECT * FROM memo WHERE id = ?";
        return  jdbcTemplate.query(sql, memoRowMapper(), id).stream().findFirst();
    }


    public RowMapper<Memo> memoRowMapper(){
        return (rs, rowNum) -> {
            Memo memo = new Memo();
            memo.setId(rs.getInt("id"));
            memo.setText(rs.getString("text"));
            return memo;
        };
    }
}
