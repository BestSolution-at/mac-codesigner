/********************************************************************************
 * Copyright (C) 2021 BestSolution.at
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * SPDX-License-Identifier: GPL-3.0-or-later
 ********************************************************************************/
package at.bestsolution.maccodesigner;

import javax.json.bind.annotation.JsonbProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class NotarizationInfo {
	@JsonbProperty("Status")
	public String status;

	@JsonbProperty("Status Message")
	public String statusMessage;

	@JsonbProperty("LogFileURL")
	public String logFileURL;
	
	@JsonbProperty("Date")
	public String date;

	@JsonbProperty("RequestUUID")
	public String requestUUID;

	@JsonbProperty("Status Code")
	public Integer statusCode;

	@JsonbProperty("Hash")
	public String hash;
	
	public NotarizationInfo() {
		
	}
}
