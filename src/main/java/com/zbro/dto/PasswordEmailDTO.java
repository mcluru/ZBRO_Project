package com.zbro.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.zbro.model.ConsumerPasswordToken;
import com.zbro.model.SellerPasswordToken;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordEmailDTO {
	
	private String userType;
	private String name;
	private String email;
	private String token;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime expiredDateString;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	private LocalDateTime createDateString;
	
	private String mailSendDomail;

	public PasswordEmailDTO(ConsumerPasswordToken passwordToken) {
		this.userType = "consumer";
		this.name = passwordToken.getUser().getName();
		this.email = passwordToken.getUser().getEmail();
		this.token = passwordToken.getToken();
		this.expiredDateString = passwordToken.getExpiredDate();
		this.createDateString = passwordToken.getCreateDate();
	}
	
	public PasswordEmailDTO(SellerPasswordToken passwordToken) {
		this.userType = "seller";
		this.name = passwordToken.getUser().getName();
		this.email = passwordToken.getUser().getEmail();
		this.token = passwordToken.getToken();
		this.expiredDateString = passwordToken.getExpiredDate();
		this.createDateString = passwordToken.getCreateDate();
	}
}
