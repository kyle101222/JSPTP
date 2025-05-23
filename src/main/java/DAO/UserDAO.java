package DAO;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import DTO.CRMUserInfoDTO;
import DTO.InquiryDTO;
import DTO.InquiryImgDTO;
import DTO.UserDTO;
import DTO.UserAddrDTO;
import DTO.OrdersDTO;
import DTO.ReviewDTO;
import DTO.ReviewImgDTO;
import DTO.UserAddrDTO;
import DTO.UserDTO;

public class UserDAO {

	private DBConnectionMgr pool;

	private final SimpleDateFormat SDF_DATE = new SimpleDateFormat("yyyy-MM-dd");

	public UserDAO() {
		pool = DBConnectionMgr.getInstance();
	}
	
	//한 사람의 정보 출력
	public UserDTO getOneUser(String id, String type) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		UserDTO user = null;
		try {
			con = pool.getConnection();
			sql = "select * from user where user_id = ? and user_type = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, type);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				user = new UserDTO(rs.getString(1), rs.getString(2), rs.getString(3), 
						rs.getString(4), rs.getString(5), rs.getString(6), rs.getInt(7), 
						rs.getInt(8), rs.getString(9), rs.getString(10), rs.getString(11), 
						rs.getString(12), rs.getString(13), rs.getString(14), rs.getString(15), 
						rs.getInt(16), rs.getString(17), rs.getString(18), rs.getInt(19), rs.getString(20));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return user;
	}

	// 회원가입
	public void insertUser(UserDTO user, UserAddrDTO addr) {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		try {
			con = pool.getConnection();
			sql = "insert user values "
					+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, '정상', ?, NULL, NULL, 0, 'N', ?, ?, '그린')";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, user.getUser_id());
			pstmt.setString(2, user.getUser_pwd());
			pstmt.setString(3, user.getUser_type());
			pstmt.setString(4, user.getUser_name());
			pstmt.setString(5, user.getUser_birth());
			pstmt.setString(6, user.getUser_gender());
			pstmt.setInt(7, user.getUser_height());
			pstmt.setInt(8, user.getUser_weight());
			pstmt.setString(9, user.getUser_email());
			pstmt.setString(10, user.getUser_phone());
			pstmt.setString(11, null);
			pstmt.setString(12, user.getUser_marketing_state());
			pstmt.setInt(13, user.getUser_point());
			pstmt.executeUpdate();
			insertAddr(addr, user.getUser_id(), user.getUser_type(), "Y");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
	}

	// 추천인 적립금
	public void updatePoint(String id, String userType) {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		try {
			con = pool.getConnection();
			sql = "UPDATE user SET user_point = user_point + 3000 WHERE user_id = ? AND user_type = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, userType);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
	}
	
	//상품 구매 시 적립금 사용 
	public void updatePointWhenOrder(int dc, String id, String type) {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		try {
			con = pool.getConnection();
			sql = "update user set user_point = user_point - ? where user_id = ? and user_type = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, dc);
			pstmt.setString(2, id);
			pstmt.setString(3, type);
			pstmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
	}
	
	//상품 구매 후 적립금 
	public void updatePointForOrder(int mileage, String id, String type) {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		try {
			con = pool.getConnection();
			sql = "update user set user_point = user_point + ? where user_id = ? and user_type = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, mileage);
			pstmt.setString(2, id);
			pstmt.setString(3, type);
			pstmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
	}


	// 아이디 중복 체크
	public boolean idCheck(String id) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		boolean flag = false;
		try {
			con = pool.getConnection();
			sql = "select user_id from user where user_id = ? and user_type = '일반'";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			rs = pstmt.executeQuery();
			if (rs.next())
				flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return flag;
	}

	// 소셜회원 중복체크
	public boolean isSocialUserExists(String id, String type) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean exists = false;

		try {
			conn = pool.getConnection(); // 기존 UserDAO에 정의된 DB 연결 메서드 사용
			String sql = "SELECT 1 FROM user WHERE user_id = ? AND user_type = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, type);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				exists = true; // 사용자 존재
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(conn, pstmt, rs);
		}
		System.out.println("id = " + id + ", type = " + type);
		System.out.println("exists = " + exists);
		return exists;
	}

	// 로그인 (success : 로그인 성공), (fail : 로그인 실패), (none : 아이디 존재 X), (resign : 탈퇴 아이디
	// 로그인), (human : 휴먼 계정), (lock : 5회이상 실패로 인한 잠금)
	public String login(String id, String pwd) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		String result = "";
		String log_type = "";
		int cnt = 0;
		String userType = "";

		try {
			con = pool.getConnection();
			if (idCheck(id)) {
				// user_type 먼저 조회
				sql = "SELECT user_type FROM user WHERE user_id = ?";
				pstmt = con.prepareStatement(sql);
				pstmt.setString(1, id);
				rs = pstmt.executeQuery();
				if (rs.next()) {
					userType = rs.getString("user_type");
				}
				rs.close();
				pstmt.close();

				// 계정 잠금 여부 확인
				if (checkLock(id, userType)) {
					String state = showAccountState(id); // 이건 그대로 둬도 됨 (기본 상태만 보여줌)
					if ("탈퇴".equals(state)) {
						result = "resign";
						return result;
					} else if ("휴먼".equals(state)) {
						result = "human";
						return result;
					}
					log_type = "잠긴 계정 로그인 시도";
					result = "lock";
				} else {
					sql = "SELECT user_id FROM user WHERE user_id = ? AND user_pwd = ?";
					pstmt = con.prepareStatement(sql);
					pstmt.setString(1, id);
					pstmt.setString(2, pwd);
					rs = pstmt.executeQuery();

					if (rs.next()) {
						result = "success";
						log_type = "로그인";
						cnt = 0;
						updateFailLogin(cnt, id, userType); // 실패횟수 초기화
					} else {
						result = "fail";
						log_type = "로그인 시도";
						cnt = showFailLogin(id, userType);
						if (cnt < 5) {
							int cnt2 = cnt + 1;
							updateFailLogin(cnt2, id, userType);
							if (cnt2 == 5) {
								updateLock("Y", id, userType);
								updateAccountState(id, userType, "로그인 연속 실패로 인한 잠금");
							}
						}
					}
				}
				insertLog(id, userType, log_type); // 로그 기록
			} else {
				result = "none";
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return result;
	}


	// 계정상태 출력
	public String showAccountState(String id) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		String state = "";
		try {
			con = pool.getConnection();
			sql = "select user_account_state from user where user_id = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			rs = pstmt.executeQuery();
			if (rs.next())
				state = rs.getString(1);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return state;
	}
	
	// 계정상태 출력(소셜로그인)
	public String showSocialAccountState(String id, String type) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		String state = "";
		try {
			con = pool.getConnection();
			sql = "select user_account_state from user where user_id = ? and user_type = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, type);
			rs = pstmt.executeQuery();
			if (rs.next())
				state = rs.getString(1);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return state;
	}

	// 계정상태 변경
	public void updateAccountState(String id, String userType, String state) {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		try {
			con = pool.getConnection();
			sql = "UPDATE user SET user_account_state = ? WHERE user_id = ? AND user_type = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, state);
			pstmt.setString(2, id);
			pstmt.setString(3, userType);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
	}


	// 계정 잠금 여부 변경
	public void updateLock(String state, String id, String userType) {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		try {
			con = pool.getConnection();
			sql = "UPDATE user SET user_lock_state = ? WHERE user_id = ? AND user_type = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, state);
			pstmt.setString(2, id);
			pstmt.setString(3, userType);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
	}


	// 계정 잠금 여부 확인 (Y: true, N: false)
	public boolean checkLock(String id, String userType) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		boolean flag = false;
		try {
			con = pool.getConnection();
			sql = "SELECT user_lock_state FROM user WHERE user_id = ? AND user_type = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, userType);
			rs = pstmt.executeQuery();
			if (rs.next() && "Y".equals(rs.getString(1))) {
				flag = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return flag;
	}


	// 로그인 잠금 실패 횟수 출력
	public int showFailLogin(String id, String userType) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		int cnt = 0;
		try {
			con = pool.getConnection();
			sql = "SELECT user_fail_login FROM user WHERE user_id = ? AND user_type = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, userType);
			rs = pstmt.executeQuery();
			if (rs.next())
				cnt = rs.getInt(1);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return cnt;
	}


	// 로그인 잠금 실패 횟수 수정
	public void updateFailLogin(int cnt, String id, String userType) {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		try {
			con = pool.getConnection();
			sql = "UPDATE user SET user_fail_login = ? WHERE user_id = ? AND user_type = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, cnt);
			pstmt.setString(2, id);
			pstmt.setString(3, userType);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
	}

	
	//계정 잠금을 풀기 위한 확인
	public boolean isLockUser(String id, String birth, String phone) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		boolean flag = false;
		try {
			con = pool.getConnection();
			sql = "select * from user where user_id = ? and user_birth = ? and user_phone = ? and user_type = '일반'";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, birth);
			pstmt.setString(3, phone);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				flag = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return flag;
	}

	// 로그아웃
	public void logout(String id, String userType) {
		insertLog(id, userType, "로그아웃");
	}


	// 로그인, 로그아웃, 로그인 시도 -> 사용자 로그 기록
	public void insertLog(String id, String userType, String logType) {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		try {
			InetAddress ip = InetAddress.getLocalHost();
			con = pool.getConnection();
			sql = "INSERT INTO user_log VALUES (null, ?, ?, now(), ?, ?)";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, userType);
			pstmt.setString(3, logType);
			pstmt.setString(4, ip.getHostAddress());
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
	}


	// 아이디 찾기 (전화번호) -> 해당하는 아이디가 있으면 (아이디, 등급, 생성일 출력), 없으면 null 리턴
	public Vector<UserDTO> findIdByPhone(String name, String phone) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		Vector<UserDTO> vlist = new Vector<UserDTO>();
		try {
			con = pool.getConnection();
			sql = "select user_id, user_rank, created_at from user "
					+ "where user_name = ? and user_phone = ? and user_type = '일반'";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setString(2, phone);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				UserDTO user = new UserDTO();
				user.setUser_id(rs.getString(1));
				user.setUser_rank(rs.getString(2));
				user.setCreated_at(SDF_DATE.format(rs.getTimestamp(3)));
				vlist.add(user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return vlist;
	}

	// 아이디 찾기 (이메일) -> 해당하는 아이디가 있으면 (아이디, 등급, 생성일 출력), 없으면 null 리턴
	public Vector<UserDTO> findIdByEmail(String name, String email) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		Vector<UserDTO> vlist = new Vector<UserDTO>();
		try {
			con = pool.getConnection();
			sql = "select user_id, user_rank, created_at from user "
					+ "where user_name = ? and user_email = ? and user_type = '일반'";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setString(2, email);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				UserDTO user = new UserDTO();
				user.setUser_id(rs.getString(1));
				user.setUser_rank(rs.getString(2));
				user.setCreated_at(SDF_DATE.format(rs.getTimestamp(3)));
				vlist.add(user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return vlist;
	}

	// 비밀번호 찾기 (전화번호)
	public boolean findPwdByPhone(String id, String name, String phone) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		boolean flag = false;
		try {
			con = pool.getConnection();
			sql = "select * from user where user_id = ? and user_name = ? and user_phone = ? and user_type = '일반'";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, name);
			pstmt.setString(3, phone);
			rs = pstmt.executeQuery();
			if (rs.next())
				flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return flag;
	}

	// 비밀번호 찾기 (이메일)
	public boolean findPwdByEmail(String id, String name, String email) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		boolean flag = false;
		try {
			con = pool.getConnection();
			sql = "select * from user where user_id = ? and user_name = ? and user_email = ? and user_type = '일반'";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, name);
			pstmt.setString(3, email);
			rs = pstmt.executeQuery();
			if (rs.next())
				flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return flag;
	}

	// 비밀번호 변경
	public boolean updatePwd(String id, String pwd) {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		boolean flag = false;
		try {
			con = pool.getConnection();
			sql = "update user set user_pwd = ? where user_id = ? and user_type='일반'";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, pwd);
			pstmt.setString(2, id);
			if(pstmt.executeUpdate() == 1)
				flag = true;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
		return flag;
	}
	
	//이름 변경
	public boolean updateName(String name, String id, String type) {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		boolean flag = false;
		try {
			con = pool.getConnection();
			sql = "update user set user_name = ? where user_id = ? and user_type = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setString(2, id);
			pstmt.setString(3, type);
			if(pstmt.executeUpdate() == 1)
				flag = true;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
		return flag;
	}
	
	//전화번호 변경
	public boolean updatePhone(String id, String phone, String type) {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		boolean flag = false;
		try {
			con = pool.getConnection();
			sql = "update user set user_phone = ? where user_id = ? and user_type = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, phone);
			pstmt.setString(2, id);
			pstmt.setString(3, type);
			if(pstmt.executeUpdate() == 1)
				flag = true;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
		return flag;
	}
	
	//이메일 변경
	public boolean updateEmail(String id, String type, String email) {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		boolean flag = false;
		try {
			con = pool.getConnection();
			sql = "update user set user_email = ? where user_id = ? and user_type = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, email);
			pstmt.setString(2, id);
			pstmt.setString(3, type);
			if(pstmt.executeUpdate() == 1)
				flag = true;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
		return flag;
	}
	
	//성별, 키, 몸무게 변경
	public boolean updateGender(String gender, int height, int weight, String id, String type) {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		boolean flag = false;
		try {
			con = pool.getConnection();
			sql = "update user set user_gender = ?, user_height = ?, user_weight = ? where user_id = ? and user_type = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, gender);
			pstmt.setInt(2, height);
			pstmt.setInt(3, weight);
			pstmt.setString(4, id);
			pstmt.setString(5, type);
			if(pstmt.executeUpdate() == 1)
				flag = true;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
		return flag;
	}
	
	//생년월일 변경
	public boolean updateBirth(String id, String type, String birth) {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		boolean flag = false;
		try {
			con = pool.getConnection();
			sql = "update user set user_birth = ? where user_id = ? and user_type = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, birth);
			pstmt.setString(2, id);
			pstmt.setString(3, type);
			if(pstmt.executeUpdate() == 1)
				flag = true;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
		return flag;
	}
	
	//일반 사용자의 비밀번호 일치 여부 확인
	public boolean isPwd(String id, String pwd) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		boolean flag = false;
		try {
			con = pool.getConnection();
			sql = "select * from user where user_id = ? and user_pwd = ? and user_type = '일반'";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, pwd);
			rs = pstmt.executeQuery();
			if(rs.next())
				flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return flag;
	}

	// 회원 수정 (비밀번호 제외)
	public void updateUser(UserDTO user, String id, String type) {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		try {
			con = pool.getConnection();
			sql = "UPDATE user SET user_name = ?, user_phone = ?, user_email = ?, "
					+ "user_gender = ?, user_height = ?, user_weight = ?, user_birth = ?, "
					+ "user_account_state = ?, user_lock_state = ?, user_marketing_state = ?, user_rank = ?, "
					+ "user_wd_date = ?, user_wd_reason = ?, user_wd_detail_reason = ? "
					+ "WHERE user_id = ? AND user_type = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, user.getUser_name());
			pstmt.setString(2, user.getUser_phone());
			pstmt.setString(3, user.getUser_email());
			pstmt.setString(4, user.getUser_gender());
			pstmt.setInt(5, user.getUser_height());
			pstmt.setInt(6, user.getUser_weight());
			pstmt.setString(7, user.getUser_birth());
			pstmt.setString(8, user.getUser_account_state());
			pstmt.setString(9, user.getUser_lock_state());
			pstmt.setString(10, user.getUser_marketing_state());
			pstmt.setString(11, user.getUser_rank());
			pstmt.setString(12, user.getUser_wd_date());
			pstmt.setString(13, user.getUser_wd_reason());
			pstmt.setString(14, user.getUser_wd_detail_reason());
			pstmt.setString(15, id);
			pstmt.setString(16, type);  // ✅ 추가
			pstmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
	}



	// 회원 수정(소셜 로그인)
	public void updateSocialUser(UserDTO user, String id) {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		try {
			con = pool.getConnection();
			sql = "update user set user_name = ?, user_phone = ?, user_email = ?, user_gender = ?, user_height = ?, user_weight = ?, user_birth = ?"
					+ "where user_id = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, user.getUser_name());
			pstmt.setString(2, user.getUser_phone());
			pstmt.setString(3, user.getUser_email());
			pstmt.setString(4, user.getUser_gender());
			pstmt.setInt(5, user.getUser_height());
			pstmt.setInt(6, user.getUser_weight());
			pstmt.setString(7, user.getUser_birth());
			pstmt.setString(8, id);
			pstmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
	}

	// 회원 탈퇴
	public boolean deleteUser(String id, String type, String reason, String detail) {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		boolean flag = false;
		try {
			con = pool.getConnection();
			sql = "update user set user_account_state = ?, user_wd_date = now(), user_lock_state = ?, user_wd_reason = ?, user_wd_detail_reason = ? where user_id = ? and user_type = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, "탈퇴");
			pstmt.setString(2, "Y");
			pstmt.setString(3, reason); // 탈퇴 사유 5개 중 하나
			pstmt.setString(4, detail); // 탈퇴 상세 사유
			pstmt.setString(5, id);
			pstmt.setString(6, type);
			if(pstmt.executeUpdate() == 1)
				flag = true;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
		return flag;
	}

	// 기본 배송지 여부 (배송지 추가 후 기본배송지로 할 경우, 기존에 기본 배송지가 있으면 N으로 변경)
	public void isDefaultAddr(String id, String type) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		try {
			con = pool.getConnection();
			sql = "select addr_id from user_address where user_id = ? and user_type = ? and addr_isDefault = 'Y'";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, type);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				int addrId = rs.getInt("addr_id");
				rs.close();
				pstmt.close();

				sql = "update user_address set addr_isDefault = 'N' where addr_id = ?";
				pstmt = con.prepareStatement(sql);
				pstmt.setInt(1, addrId);
				pstmt.executeUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
	}

	// 주소 입력
	public void insertAddr(UserAddrDTO addr, String id, String type, String isDefault) {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		try {
			if (isDefault.equals("Y")) {
				isDefaultAddr(id, type);
			}
			con = pool.getConnection();
			sql = "insert user_address values (null, ?, ?, ?, ?, ?, ?, now(), ?)";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, type);
			pstmt.setString(3, addr.getAddr_zipcode());
			pstmt.setString(4, addr.getAddr_road());
			pstmt.setString(5, addr.getAddr_detail());
			pstmt.setString(6, isDefault);
			pstmt.setString(7, (addr.getAddr_label() == null || addr.getAddr_label() == "") ? addr.getAddr_road()
					: addr.getAddr_label());
			pstmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
	}

	// 전체 배송지 출력 (기본 배송지가 가장 먼저 나오고 나머지 주소들은 생성일 순서대로 출력)
	public Vector<UserAddrDTO> showAllAddr(String id, String type) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		Vector<UserAddrDTO> vlist = new Vector<UserAddrDTO>();
		try {
			con = pool.getConnection();
			sql = "select * from user_address where user_id = ? and user_type = ? order by (addr_isDefault = 'Y') desc, created_at desc";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, type);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				vlist.add(new UserAddrDTO(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
						rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return vlist;
	}

	// 기본 배송지 출력
	public UserAddrDTO showOneAddr(String id, String type) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		UserAddrDTO addr = null;
		try {
			con = pool.getConnection();
			sql = "select * from user_address where user_id = ? and user_type = ? and addr_isDefault = 'Y'";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, type);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				addr = new UserAddrDTO(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),
						rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return addr;
	}

	// 기본배송지 제외 리스트 출력
	public Vector<UserAddrDTO> showRestAddr(String id, String type) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		Vector<UserAddrDTO> vlist = new Vector<UserAddrDTO>();
		try {
			con = pool.getConnection();
			sql = "select * from user_address where user_id = ? and user_type = ? and addr_isDefault = 'N' order by created_at desc";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, type);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				vlist.add(new UserAddrDTO(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
						rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9)));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return vlist;
	}

	// 배송지 수정
	public void updateAddr(String id, String type, int addr_id, UserAddrDTO addr) {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		try {
			if (addr.getAddr_isDefault().equals("Y"))
				isDefaultAddr(id, type);
			con = pool.getConnection();
			sql = "update user_address set "
					+ "addr_zipcode = ?, addr_road = ?, addr_detail = ?, addr_isDefault = ?, addr_label = ? where addr_id = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, addr.getAddr_zipcode());
			pstmt.setString(2, addr.getAddr_road());
			pstmt.setString(3, addr.getAddr_detail());
			pstmt.setString(4, addr.getAddr_isDefault());
			pstmt.setString(5, (addr.getAddr_label() == null || addr.getAddr_label() == "") ? addr.getAddr_road()
					: addr.getAddr_label());
			pstmt.setInt(6, addr_id);
			pstmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
	}

	// 배송지 삭제 (기본 배송지 : false, 나머지 배송지 : true)
	public boolean deleteAddr(int addr_id) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		boolean flag = false;
		try {
			con = pool.getConnection();
			sql = "select addr_isDefault from user_address where addr_id = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, addr_id);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				String isDefault = rs.getString(1);
				rs.close();
				pstmt.close();

				if (isDefault.equals("Y"))
					return false;
				else {
					sql = "delete from user_address where addr_id = ?";
					pstmt = con.prepareStatement(sql);
					pstmt.setInt(1, addr_id);
					if (pstmt.executeUpdate() == 1)
						flag = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return flag;
	}
	
	// 주소id 하나에 대한 주소정보
	public UserAddrDTO getAddrById(int addr_id) {
	    Connection con = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;
	    String sql;
	    UserAddrDTO addr = null;
	    try {
	        con = pool.getConnection();
	        sql = "SELECT * FROM user_address WHERE addr_id = ?";
	        pstmt = con.prepareStatement(sql);
	        pstmt.setInt(1, addr_id);
	        rs = pstmt.executeQuery();
	        if (rs.next()) {
	            addr = new UserAddrDTO(
	                rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
	                rs.getString(5), rs.getString(6), rs.getString(7),
	                rs.getString(8), rs.getString(9)
	            );
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        pool.freeConnection(con, pstmt, rs);
	    }
	    return addr;
	}

	
	//한 유저의 미사용 쿠폰 수 출력
	public int showOneUserCoupon(String id, String type) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		int cnt = 0;
		try {
			con = pool.getConnection();
			sql = "select count(*) from user_coupon where user_id = ? and user_type = ? and cp_using_state = 'N'";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, type);
			rs = pstmt.executeQuery();
			while(rs.next())
				cnt = rs.getInt(1);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return cnt;
	}

	// 신규주문 수
	public int getTodayOrderCount() {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int count = 0;

		try {
			con = pool.getConnection();
			String sql = "SELECT COUNT(*) FROM orders WHERE DATE(created_at) = CURDATE()";
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return count;
	}

	// 신규주문 리스트 (페이징)
	public List<OrdersDTO> getTodayOrderList(int page, int pageSize) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<OrdersDTO> list = new ArrayList<>();
		int offset = (page - 1) * pageSize;

		try {
			con = pool.getConnection();
			String sql = "SELECT * FROM orders WHERE DATE(created_at) = CURDATE() ORDER BY created_at DESC LIMIT ?, ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, offset);
			pstmt.setInt(2, pageSize);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				OrdersDTO order = new OrdersDTO();
				order.setO_id(rs.getInt("o_id"));
				order.setUser_id(rs.getString("user_id"));
				order.setPd_id(rs.getInt("pd_id"));
				order.setO_num(rs.getString("o_num"));
				order.setO_isMember(rs.getString("o_isMember"));
				order.setO_name(rs.getString("o_name"));
				order.setO_phone(rs.getString("o_phone"));
				order.setQuantity(rs.getInt("o_quantity"));
				order.setCreated_at(rs.getString("created_at"));
				order.setO_total_amount(rs.getInt("o_total_amount"));
				order.setPay_id(rs.getInt("pay_id"));
				order.setRf_id(rs.getInt("rf_id"));

				list.add(order);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return list;
	}

	// 신규회원수 : 오늘 가입한 사람 COUNT
	public int getTodayNewUserCount() {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int count = 0;

		try {
			con = pool.getConnection();
			String sql = "SELECT COUNT(*) FROM user WHERE DATE(created_at) = CURDATE() AND user_account_state != '탈퇴'";
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}

		return count;
	}

	// 신규회원 리스트 : 오늘 가입한 사람 목록 → 페이징처리 목적(LIMIT처리)
	public List<UserDTO> getTodayNewUserList(int page, int pageSize) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<UserDTO> list = new ArrayList<>();

		int offset = (page - 1) * pageSize;

		try {
			con = pool.getConnection();
			String sql = "SELECT * FROM user WHERE DATE(created_at) = CURDATE() " + "AND user_account_state != '탈퇴' "
					+ "ORDER BY created_at DESC LIMIT ?, ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, offset);
			pstmt.setInt(2, pageSize);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				UserDTO user = new UserDTO();
				user.setUser_id(rs.getString("user_id"));
				user.setUser_type(rs.getString("user_type"));
				user.setUser_name(rs.getString("user_name"));
				user.setUser_gender(rs.getString("user_gender"));
				user.setCreated_at(rs.getString("created_at"));
				user.setUser_marketing_state(rs.getString("user_marketing_state"));
				list.add(user);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}

		return list;
	}

	// 탈퇴회원수
	public int getWithdrawalUserCount() {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int count = 0;

		try {
			con = pool.getConnection();
			String sql = "SELECT COUNT(*) FROM user WHERE user_account_state = '탈퇴'";
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}

		return count;
	}

	// 기존 방식 (전체 탈퇴회원 조회) → 오버로딩
	public List<UserDTO> getWithdrawalUserList(int page, int pageSize) {
		return getWithdrawalUserList(page, pageSize, null); // 새로 만든 메서드에 null 넘김
	}

	// 탈퇴회원 리스트 + 정렬기능
	public List<UserDTO> getWithdrawalUserList(int page, int pageSize, String reason) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<UserDTO> list = new ArrayList<>();
		int offset = (page - 1) * pageSize;

		try {
			con = pool.getConnection();

			String sql = "SELECT * FROM user WHERE user_account_state = '탈퇴'";
			if (reason != null && !reason.trim().isEmpty()) {
				sql += " AND user_wd_reason = ?";
			}
			sql += " ORDER BY user_wd_date DESC LIMIT ?, ?";

			pstmt = con.prepareStatement(sql);

			int paramIndex = 1;
			if (reason != null && !reason.trim().isEmpty()) {
				pstmt.setString(paramIndex++, reason);
			}
			pstmt.setInt(paramIndex++, offset);
			pstmt.setInt(paramIndex, pageSize);

			rs = pstmt.executeQuery();
			while (rs.next()) {
				String uid = rs.getString("user_id");
				UserDTO user = new UserDTO();
				user.setUser_id(uid);
				user.setUser_name(rs.getString("user_name"));
				user.setUser_rank(rs.getString("user_rank"));
				user.setUser_type(rs.getString("user_type"));
				user.setUser_wd_date(rs.getString("user_wd_date"));
				user.setUser_wd_reason(rs.getString("user_wd_reason"));
				user.setUser_wd_detail_reason(rs.getString("user_wd_detail_reason"));

				list.add(user);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return list;
	}

	// 탈퇴회원 상세정보
	public UserDTO getWithdrawalDetail(String id) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		UserDTO user = null;

		try {
			con = pool.getConnection();
			String sql = "SELECT * FROM user WHERE user_id = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				user = new UserDTO();
				user.setUser_id(rs.getString("user_id"));
				user.setUser_name(rs.getString("user_name"));
				user.setUser_rank(rs.getString("user_rank"));
				user.setCreated_at(rs.getString("created_at"));
				user.setUser_wd_date(rs.getString("user_wd_date"));
				user.setUser_wd_reason(rs.getString("user_wd_reason"));
				user.setUser_wd_detail_reason(rs.getString("user_wd_detail_reason"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return user;
	}

	// 전체회원수
	public int getTotalUserCount() {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int count = 0;

		try {
			con = pool.getConnection();
			String sql = "SELECT COUNT(*) FROM user WHERE user_account_state NOT IN ('탈퇴')";
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}

		return count;
	}

	// 전체회원 리스트
	public List<UserDTO> getTotalUserList(int page, int pageSize) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<UserDTO> list = new ArrayList<>();
		int offset = (page - 1) * pageSize;

		try {
			con = pool.getConnection();
			String sql = "SELECT user_id, user_name, user_type, user_rank, created_at, "
					+ "user_account_state, user_point, user_marketing_state " + "FROM user "
					+ "WHERE user_account_state NOT IN ('탈퇴') " + "ORDER BY created_at DESC " + "LIMIT ?, ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, offset);
			pstmt.setInt(2, pageSize);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				UserDTO user = new UserDTO();
				user.setUser_id(rs.getString("user_id"));
				user.setUser_name(rs.getString("user_name"));
				user.setUser_type(rs.getString("user_type"));
				user.setUser_rank(rs.getString("user_rank"));
				user.setCreated_at(rs.getString("created_at"));
				user.setUser_account_state(rs.getString("user_account_state"));
				user.setUser_point(rs.getInt("user_point"));
				user.setUser_marketing_state(rs.getString("user_marketing_state"));
				list.add(user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}
		return list;
	}

	// 회원정보 수정을 위한 전체 회원정보 가져오기
	public UserDTO getUserById(String id, String type) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		UserDTO user = null;

		try {
			con = pool.getConnection();
			String sql = "SELECT * FROM user WHERE user_id = ? AND user_type = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			pstmt.setString(2, type);
			rs = pstmt.executeQuery();

			if (rs.next()) {
				user = new UserDTO();
				user.setUser_id(rs.getString("user_id"));
				user.setUser_pwd(rs.getString("user_pwd"));
				user.setUser_type(rs.getString("user_type"));
				user.setUser_name(rs.getString("user_name"));
				user.setUser_birth(rs.getString("user_birth"));
				user.setUser_gender(rs.getString("user_gender"));
				user.setUser_height(rs.getInt("user_height"));
				user.setUser_weight(rs.getInt("user_weight"));
				user.setUser_email(rs.getString("user_email"));
				user.setCreated_at(rs.getString("created_at"));
				user.setUser_phone(rs.getString("user_phone"));
				user.setUser_account_state(rs.getString("user_account_state"));
				user.setUser_wd_date(rs.getString("user_wd_date"));
				user.setUser_wd_reason(rs.getString("user_wd_reason"));
				user.setUser_wd_detail_reason(rs.getString("user_wd_detail_reason"));
				user.setUser_fail_login(rs.getInt("user_fail_login"));
				user.setUser_lock_state(rs.getString("user_lock_state").trim().toUpperCase());
				user.setUser_marketing_state(rs.getString("user_marketing_state").trim().toUpperCase());
				user.setUser_point(rs.getInt("user_point"));
				user.setUser_rank(rs.getString("user_rank"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}

		return user;
	}

	// CRM 고객 관계 관리 → 한 명의 회원에 대한 전체 정보를 관리할 메소드
	public CRMUserInfoDTO getCRMUserInfo(String user_id, String user_type) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		CRMUserInfoDTO crmInfo = new CRMUserInfoDTO();
		try {
			con = pool.getConnection();

			// 1. 기본 회원 정보
			UserDTO user = getUserById(user_id, user_type);
			crmInfo.setUser(user);

			// 2. 기본 배송지
			UserAddrDTO addr = showOneAddr(user_id, user_type);
			crmInfo.setAddr(addr);

			// 3. 누적 결제 금액
			String sqlTotal = "SELECT IFNULL(SUM(o_total_amount), 0) FROM orders WHERE user_id = ?";
			pstmt = con.prepareStatement(sqlTotal);
			pstmt.setString(1, user_id);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				crmInfo.setTotalOrderAmount(rs.getInt(1));
			}
			rs.close();
			pstmt.close();

			// 4. 최근 로그인 일자
			String sqlLogin = "SELECT MAX(log_date) FROM user_log WHERE user_id = ? AND log_type = '로그인'";
			pstmt = con.prepareStatement(sqlLogin);
			pstmt.setString(1, user_id);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				crmInfo.setLastLoginDate(rs.getString(1)); // null 처리 X: JSP에서 "-" 처리
			}
			rs.close();
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}

		return crmInfo;
	}

	// 탈퇴회원 정상회원으로 복구
	public void restoreWithdrawnUser(String user_id) {
		Connection con = null;
		PreparedStatement pstmt = null;
		String sql = null;
		try {
			con = pool.getConnection();
			sql = "UPDATE user SET user_account_state = '정상', user_lock_state = 'N', user_fail_login = 0, "
					+ "user_wd_date = NULL, user_wd_reason = NULL, user_wd_detail_reason = NULL " + "WHERE user_id = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, user_id);
			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt);
		}
	}
	



	// 특정 회원 문의 가져오기
	public List<InquiryDTO> getInquiriesByUser(String id) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<InquiryDTO> list = new ArrayList<>();

		try {
			con = pool.getConnection();
			String sql = "SELECT * FROM inquiry WHERE user_id = ? ORDER BY created_at DESC";
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, id);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				InquiryDTO dto = new InquiryDTO();
				dto.setI_id(rs.getInt("i_id"));
				dto.setUser_id(rs.getString("user_id"));
				dto.setP_id(rs.getInt("p_id"));
				dto.setO_id(rs.getInt("o_id"));
				dto.setI_title(rs.getString("i_title"));
				dto.setI_content(rs.getString("i_content"));
				dto.setCreated_at(rs.getString("created_at"));
				dto.setI_isPrivate(rs.getString("i_isPrivate"));
				dto.setI_status(rs.getString("i_status"));
				list.add(dto);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}

		return list;
	}
	
	// 문의 이미지 조회
	public List<InquiryImgDTO> getInquiryImages(int i_id) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<InquiryImgDTO> list = new ArrayList<>();

		try {
			con = pool.getConnection();
			String sql = "SELECT * FROM inquiry_image WHERE i_id = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, i_id);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				InquiryImgDTO img = new InquiryImgDTO();
				img.setIi_id(rs.getInt("ii_id"));
				img.setI_id(rs.getInt("i_id"));
				img.setIi_url(rs.getString("ii_url"));
				img.setUploaded_at(rs.getString("uploaded_at"));
				list.add(img);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.freeConnection(con, pstmt, rs);
		}

		return list;
	}



}

