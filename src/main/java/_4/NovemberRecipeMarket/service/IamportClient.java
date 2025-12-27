package _4.NovemberRecipeMarket.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.Map;

@Slf4j
@Service
public class IamportClient {
    @Value("${IMP_KEY}")
    private String impKEY;

    @Value("${IMP_SECRET}")
    private String impSecret;


    public String getToken() throws IOException {
        // JDK 개본 HttpsURLConnection을 이용한 HTTP 통신 (스프링 RestClient 대신 사용)
        HttpsURLConnection conn = null;

        URL url = new URL("https://api.iamport.kr/users/getToken");

        conn = (HttpsURLConnection) url.openConnection();

        // Header 설정
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        // Request body에 담아 아임포트 서버로 보낼 imp_key와 imp_secret
        JsonObject json = new JsonObject();
        json.addProperty("imp_key", impKEY);
        json.addProperty("imp_secret", impSecret);

        // Request Body를 아임프토 서버로 보내기
        // IMP_KEY와 IMP_SECRET으로 토큰을 달라는 요청을 보내는 것
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
        bw.write(json.toString());
        bw.flush(); // 지금까지 쓴 데이터를 실제로 전송
        bw.close(); // Request Body 전송이 끝남을 알림

        // 아임포트 서버의 응답을 읽는 부분
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        /*
            - JSON 직렬화/역직렬화 library
            - Java 객체 -> JSON 문제열 (and vice versa)
            - HTTP 통신은 안함. JSON parsing만 담당
         */
        Gson gson = new Gson();

        /*
            실제 서버 응답 예시
            {
              "code": 0,
              "message": "success",
              "response": {
                "access_token": "eyJhbGciOiJIUzI1NiJ9...",
                "expired_at": 1700000000
              }
            }
         */
        // 서버가 보낸 JSON 문자열 한 줄 읽어 Map으로 파싱
        String response = gson.fromJson(br.readLine(), Map.class).get("response").toString();

        // response 객체 안에 포함된 acces_token만 추출
        String  token = gson.fromJson(response, Map.class).get("access_token").toString();

        br.close();
        conn.disconnect();

        return token;
    }

    /* GET https://api.iamport.kr/payments/{imp_uid}
        Authorization header에 토큰 넣고 조회
        응답 JSON을 Response.class 파싱해서 response.amount 반환
     */

    public int paymentInfo(String imp_Uid, String token) throws IOException {
        HttpsURLConnection conn = null;
        URL url = new URL("https://api.iamport.kr/payments/" + imp_Uid);

        conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", token);

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
        Gson gson = new Gson();
        IamportResponse response = gson.fromJson(br.readLine(), IamportResponse.class);

        br.close();
        conn.disconnect();
        return response.getResponse().getAmount();
    }


    //
    /*
        Resonse Body로 받을 JSON은 이런 형식으로, "response"안에 "amount가 있다.
        amount를 추출하기 위해서는 wrapper 클래스를 만들어 response body 전체를 받고,
        그 안에 있는 amount만 매핑하는 DTO를 사용해 파싱한다
        {
          "code": 0,
          "message": "...",
          "response": {
            "amount": 10000
            }
        }
     */
    @Getter
    class IamportResponse<T> {
        private PaymentInfo response;
    }

    @Getter
    class PaymentInfo {
        private int amount;
    }

    public void cancelPayment(String token, String imp_Uid, int amount, String reason) throws IOException{
        HttpsURLConnection conn = null;
        URL url = new URL("https://api.iamport.kr/payments/cancel");
        conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-type", "application/json");
        conn.setRequestProperty("Accept", "application.json");
        conn.setRequestProperty("Authorization", token);
        conn.setDoOutput(true);

        JsonObject json = new JsonObject();
        json.addProperty("reason", reason);
        json.addProperty("imp_uid", imp_Uid);
        json.addProperty("amount", amount);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
        bw.write(json.toString());
        bw.flush();
        bw.close();;

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
        br.close();
        conn.disconnect();

    }
}
