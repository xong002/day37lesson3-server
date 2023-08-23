package lesson.day37lesson3.repositories;

import java.io.InputStream;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lesson.day37lesson3.models.UploadContent;

@Repository
public class BlobRepository {

    private static final String SQL_INSERT_INTO_UPLOADS = "insert into myuploads(description, content, media_type) values (?,?,?)";
    private static final String SQL_GETBYID = "select * from myuploads where id = ?";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Blob content is in the form of InputStream
    public void upload(String description, InputStream is, String mediaType) {
        jdbcTemplate.update(SQL_INSERT_INTO_UPLOADS, description, is, mediaType);
    }

    public Optional<UploadContent> getById(Integer id) {

        LinkedList<UploadContent> resultList = jdbcTemplate.query(SQL_GETBYID, (ResultSet rs) -> {
            LinkedList<UploadContent> list = new LinkedList<>();
            while (rs.next()) {
                System.out.println("rs: " + rs.toString());
                UploadContent content = new UploadContent(rs.getInt("id"),
                        rs.getString("description"),
                        rs.getString("media_type"),
                        rs.getBytes("content"));

                list.add(content);
            }
            return list;
        }, id);

        if (resultList.get(0) == null) {
            return Optional.empty();
        }
        return Optional.of(resultList.get(0));
    }

}
