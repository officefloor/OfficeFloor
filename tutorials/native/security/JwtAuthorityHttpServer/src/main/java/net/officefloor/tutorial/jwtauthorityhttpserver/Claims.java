package net.officefloor.tutorial.jwtauthorityhttpserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Claims {
	private String id;
	private int randomValueToMakeAccessTokensDifferent;
	private String[] roles;
}