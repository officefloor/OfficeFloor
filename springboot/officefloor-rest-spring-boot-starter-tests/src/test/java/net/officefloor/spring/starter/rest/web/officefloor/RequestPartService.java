/*-
 * #%L
 * OfficeFloor REST Spring Boot Starter
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
