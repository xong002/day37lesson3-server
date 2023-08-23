package lesson.day37lesson3.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.amazonaws.services.s3.transfer.Upload;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import lesson.day37lesson3.models.UploadContent;
import lesson.day37lesson3.repositories.BlobRepository;
import lesson.day37lesson3.repositories.S3Repository;

@Controller
@RequestMapping
public class UploadController {

    @Autowired
    private BlobRepository blobRepo;

    @Autowired
    private S3Repository s3Repo;

    @GetMapping(path = "/image/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@PathVariable Integer id) {

        Optional<UploadContent> opt = blobRepo.getById(id);

        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(200)
                .contentType(MediaType.valueOf(opt.get().contentType()))
                .contentLength(opt.get().content().length)
                .body(opt.get().content());
    }


    @GetMapping(path = "/upload/{id}")
    public ModelAndView getById(@PathVariable Integer id) {
        ModelAndView mav = new ModelAndView();

        Optional<UploadContent> uploadContent = blobRepo.getById(id);

        if (uploadContent.isEmpty()) {
            mav.setViewName("error");
            mav.addObject("error", "No file found.");
        }
        mav.setViewName("content");
        mav.addObject("description", uploadContent.get().description());
        mav.addObject("image", uploadContent.get().content()); // change uploadContent.content() to request to
                                                               // GetMapping for
        // /image
        return mav;
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<String> postUploadReturnJson(@RequestPart String description,
            @RequestPart MultipartFile myfile) {
        String mediaType = myfile.getContentType();
        try {
            InputStream is = myfile.getInputStream();
            blobRepo.upload(description, is, mediaType);

            // uploading to s3
            String id = s3Repo.saveImage(myfile);
            System.out.println("Generated ID in S3: " + id);

            JsonObject jsonObj = Json.createObjectBuilder().add("id", id).build();
            return ResponseEntity.ok().body(jsonObj.toString());

        } catch (IOException ex) {
            return ResponseEntity.badRequest().build();
        }

    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ModelAndView postUpload(@RequestPart String description, @RequestPart MultipartFile myfile) {

        ModelAndView mav = new ModelAndView();

        String mediaType = myfile.getContentType();
        try {
            InputStream is = myfile.getInputStream();
            blobRepo.upload(description, is, mediaType);

            // uploading to s3
            String id = s3Repo.saveImage(myfile);
            System.out.println("Generated ID in S3: " + id);

        } catch (IOException ex) {
            mav.setStatus(HttpStatusCode.valueOf(400));
            mav.setViewName("ERROR");
            mav.addObject("error", ex.getMessage());
            return mav;
        }

        mav.addObject("controlName", myfile.getName());
        mav.addObject("fileName", myfile.getOriginalFilename());
        mav.addObject("mediaType", myfile.getContentType());
        mav.addObject("fileSize", myfile.getSize());
        mav.setViewName("upload");

        return mav;
    }

}
