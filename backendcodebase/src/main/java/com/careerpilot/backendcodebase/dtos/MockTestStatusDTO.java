
package com.careerpilot.backendcodebase.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MockTestStatusDTO {

    private String mockTestId;

    private Long attemptId;

    private String status;
}
