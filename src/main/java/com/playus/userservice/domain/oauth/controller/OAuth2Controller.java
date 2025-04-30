//package com.playus.userservice.domain.oauth.controller;
//
//import com.playus.userservice.domain.oauth.service.OAuth2Service;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.Map;
//
//@Slf4j
//@RestController
//@RequestMapping("/user/login/kakao")
//@RequiredArgsConstructor
//public class OAuth2Controller {
//
//    private final OAuth2Service oAuth2Service;
//    @GetMapping("/kakao")
//    public ResponseEntity<Map<String, String>> kakaoCallback(@RequestParam String code) {
//        Map<String, String> response = oAuth2Service.kakaoLogin(code);
//        return ResponseEntity.ok(response);
//    }
//}
