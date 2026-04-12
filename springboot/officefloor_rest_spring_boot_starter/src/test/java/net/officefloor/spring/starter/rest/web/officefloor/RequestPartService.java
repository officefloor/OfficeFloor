package net.officefloor.spring.starter.rest.web.officefloor;

import net.officefloor.web.ObjectResponse;
import org.mockito.internal.util.io.IOUtil;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.stream.Collectors;

public class RequestPartService {
    public void service(@RequestPart(name = "file") MultipartFile file, ObjectResponse<String> response) throws IOException {
        String content = IOUtil.readLines(file.getInputStream()).stream().collect(Collectors.joining());
        response.send("file=" + file.getOriginalFilename() + ", content=" + content);
    }
}
