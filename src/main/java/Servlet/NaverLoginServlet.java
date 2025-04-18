package Servlet;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.net.*;

import org.json.JSONObject;

import DAO.UserDAO;

@WebServlet("/NaverLoginServlet")
public class NaverLoginServlet extends HttpServlet {
	private final String clientId = System.getenv("NAVER_CLIENT_ID");
    private final String clientSecret = System.getenv("NAVER_CLIENT_SECRET");
    private final String redirectURI = "http://everywear.duckdns.org/JSPTP/NaverLoginServlet";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String code = request.getParameter("code");
        String state = request.getParameter("state");
        String sessionState = (String) request.getSession().getAttribute("state");

        if (!state.equals(sessionState)) {
            response.getWriter().println("잘못된 접근입니다. (CSRF 차단)");
            return;
        }

        // 1. access_token 요청
        String tokenURL = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code"
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&redirect_uri=" + URLEncoder.encode(redirectURI, "UTF-8")
                + "&code=" + code
                + "&state=" + state;

        try {
            String tokenResponse = get(tokenURL);
            JSONObject tokenObj = new JSONObject(tokenResponse);
            
            if (!tokenObj.has("access_token")) {
                System.out.println("access_token not found in response: " + tokenResponse);
                response.getWriter().println("네이버 로그인 인증 중 문제가 발생했습니다.");
                return;
            }
            
            String accessToken = tokenObj.getString("access_token");
    
            // 2. 사용자 정보 요청
            String userInfoURL = "https://openapi.naver.com/v1/nid/me";
            String userInfoResponse = get(userInfoURL, "Bearer " + accessToken);
    
            JSONObject userObj = new JSONObject(userInfoResponse);
            JSONObject res = userObj.getJSONObject("response");
    
            String id = res.getString("id");
            String name = res.optString("name", ""); 
            String email = res.optString("email", "");
    
            // 3. 세션에 저장
            HttpSession session = request.getSession();
            session.setAttribute("naverId", id);
            session.setAttribute("naverName", name);
            session.setAttribute("naverEmail", email);
    
            // 4. 로그인 완료 후 메인 페이지로 이동
            response.sendRedirect("main2.jsp");
        } catch (Exception e) {
            System.out.println("Error during Naver login: " + e.getMessage());
            e.printStackTrace();
            response.getWriter().println("네이버 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private String get(String apiURL) throws IOException {
        return get(apiURL, null);
    }

    private String get(String apiURL, String authHeader) throws IOException {
        URL url = new URL(apiURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        if (authHeader != null) {
            con.setRequestProperty("Authorization", authHeader);
        }

        int responseCode = con.getResponseCode();
        
        BufferedReader br;
        if (responseCode == 200) { // 정상 호출
            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {  // 에러 발생
            br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }
        
        String inputLine;
        StringBuilder responseBuffer = new StringBuilder();
        while ((inputLine = br.readLine()) != null) {
            responseBuffer.append(inputLine);
        }
        br.close();
        
        String responseContent = responseBuffer.toString();
        System.out.println("API Response (" + apiURL + "): " + responseContent);
        
        return responseContent;
    }
}
