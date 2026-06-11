/*-
 * #%L
 * Team
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

package net.officefloor.activity.team;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Configuration for an {@link net.officefloor.compile.spi.officefloor.OfficeFloorTeam}.
 */
@Data
public class TeamConfiguration {

    private String source;

    @JsonProperty("team-size")
    private int teamSize;

    private Map<String, String> properties;

    /**
     * Convenience shorthand for a single unqualified type qualification.
     */
    private String type;

    /**
     * Multiple type qualifications (qualifier + type pairs).
     */
    @JsonProperty("type-qualifications")
    private List<TypeQualificationConfiguration> typeQualifications;

}
