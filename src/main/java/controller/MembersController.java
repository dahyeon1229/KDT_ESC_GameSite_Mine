package controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import commons.EncryptionUtils;
import dao.MembersDAO;
import dto.MembersDTO;

@WebServlet("*.members")
public class MembersController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String cmd = request.getRequestURI();
		MembersDAO dao = MembersDAO.getInstance();
		Long currentTime = System.currentTimeMillis();
		System.out.println(cmd);
		try {
			if(cmd.equals("/Membership.members")) { // 회원가입 페이지로 이동
				response.sendRedirect("/member.jsp");
			}
			else if(cmd.equals("/AcceptLogin.members")) { // 회원가입 후 index.jsp로 이동
				request.setCharacterEncoding("UTF-8");
				String id = request.getParameter("id");
				String pw = request.getParameter("pw");
				String shapw = EncryptionUtils.getSHA512(pw);
				String name = request.getParameter("name");
				String phone = request.getParameter("phone");
				String email = request.getParameter("email");
				String zipcode = request.getParameter("zipcode");
				String address1 = request.getParameter("address1");
				String address2 = request.getParameter("address2");
				Timestamp regdate = new Timestamp(currentTime);
				dao.insert(new MembersDTO(id, shapw, name, phone, email, zipcode, address1, address2, regdate));
				response.sendRedirect("/index.jsp");
			}
			else if(cmd.equals("/idcheck.members")){ // 아이디 중복체크 ajax
				Gson gson = new Gson();
				String id = request.getParameter("id");
				Boolean result = dao.isIdExist(new MembersDTO(id));
				String json = gson.toJson(result);
				PrintWriter pw = response.getWriter();
				pw.append(json);
			}
			else if(cmd.equals("/login.members")){
				String id = request.getParameter("id");
				String pw = request.getParameter("pw");
				pw = EncryptionUtils.getSHA512(pw);
				boolean success = dao.login(id, pw);
				if(success) {
					request.getSession().setAttribute("loginID",id);
					MembersDTO dto = dao.mypage(id);
					request.setAttribute("name",dto.getName());
					request.setAttribute("email",dto.getEmail());
				}
				request.getRequestDispatcher("/index.jsp").forward(request,response); // 로그인시 어디로 이동할지
			}

			else if(cmd.equals("/logout.members")) {
				request.getSession().invalidate();
				response.sendRedirect("/index.jsp"); // 로그아웃시 어디로 이동할지
			}
			else if(cmd.equals("/mypage.members")) {
				String id = (String)request.getSession().getAttribute("loginID");
				MembersDTO dto = dao.mypage(id);
				request.setAttribute("dto", dto);
				request.getRequestDispatcher("/myPage.jsp").forward(request,response);
			} else if (cmd.equals("/update.members")) {
				response.sendRedirect("/updateMyPage.jsp");
			} else if (cmd.equals("/updateComplete.members")) {
				request.setCharacterEncoding("UTF-8");
				String id = (String)request.getSession().getAttribute("loginID");
				String name = request.getParameter("name");
				String phone1 = request.getParameter("phone1");
				String phone2 = request.getParameter("phone2");
				String phone = "010"+phone1 + phone2;
				String email1 = request.getParameter("email1");
				String email2 = request.getParameter("email2");
				String email = email1 + "@" + email2;
				String zipcode = request.getParameter("zipcode");
				String address1 = request.getParameter("address1");
				String address2 = request.getParameter("address2");
				String address = address1 + " " +address2;
				String address3 = request.getParameter("address3");

				dao.update(id, name, phone, email, zipcode, address, address3);
				MembersDTO dto = dao.mypage(id);
				request.setAttribute("dto", dto);
				request.getRequestDispatcher("/myPage.jsp").forward(request, response);
			} else if(cmd.equals("/updateBack.members")) {
				String id = (String)request.getSession().getAttribute("loginID");
				MembersDTO dto = dao.mypage(id);
				request.setAttribute("name",dto.getName());
				request.setAttribute("email",dto.getEmail());
				request.getRequestDispatcher("/index.jsp").forward(request,response);
			}
		} catch (Exception e) {
			response.sendRedirect("/error.html");
			e.printStackTrace();
		}
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
