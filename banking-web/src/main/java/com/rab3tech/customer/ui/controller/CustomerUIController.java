package com.rab3tech.customer.ui.controller;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.rab3tech.customer.service.CustomerService;
import com.rab3tech.customer.service.LoginService;
import com.rab3tech.customer.service.PayeeInfoService;
import com.rab3tech.customer.service.TransactionService;
import com.rab3tech.customer.service.impl.CustomerEnquiryService;
import com.rab3tech.customer.service.impl.SecurityQuestionService;
//import com.rab3tech.dao.entity.SecurityQuestions;
import com.rab3tech.email.service.EmailService;
import com.rab3tech.vo.ApplicationResponseVO;
import com.rab3tech.vo.ChangePasswordVO;
//import com.rab3tech.vo.CustomerAccountInfoVO;
import com.rab3tech.vo.CustomerSavingVO;
import com.rab3tech.vo.CustomerSecurityQueAnsVO;
import com.rab3tech.vo.CustomerVO;
import com.rab3tech.vo.EmailVO;
import com.rab3tech.vo.LoginVO;
import com.rab3tech.vo.PayeeInfoVO;
import com.rab3tech.vo.SecurityQuestionsVO;
import com.rab3tech.vo.TransactionVO;

@Controller
public class CustomerUIController {

	private static final Logger logger = LoggerFactory.getLogger(CustomerUIController.class);

	@Autowired
	private PayeeInfoService payeeInfoService;

	@Autowired
	private CustomerEnquiryService customerEnquiryService;

	@Autowired
	private SecurityQuestionService securityQuestionService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private LoginService loginService;
	
	@Autowired
	private TransactionService transactionService;

	@PostMapping("/customer/account/fundtransfer")
	public String verifyTransfer(@ModelAttribute TransactionVO transcationVo, HttpSession session, Model model){
		logger.debug("verifyTransfer");
		LoginVO loginVO = (LoginVO) session.getAttribute("userSessionVO");
		if (loginVO == null) {
			model.addAttribute("error", "please Login first");
			return "customer/login";
		} else {
			transcationVo.setCustomerId(loginVO.getUsername());
			String response = transactionService.trasferCheck(transcationVo);
			List<PayeeInfoVO> payees = payeeInfoService.getAllPayee(loginVO.getUsername());
			model.addAttribute("message", response);	
			model.addAttribute("payeeInfoList", payees);
			return "customer/fundTransfer";
			
		}
		
	} 
	
	@GetMapping("/customer/account/fundtransfer")
	public String fundTransfer(HttpSession session, Model model){
		logger.debug("fundTransfer");
		LoginVO loginVO = (LoginVO) session.getAttribute("userSessionVO");
		if (loginVO == null) {
			model.addAttribute("error", "please Login first");
			return "customer/login";
		} else {
			List<PayeeInfoVO> payees = payeeInfoService.getAllPayee(loginVO.getUsername());
			model.addAttribute("payeeInfoList", payees);
			return "customer/fundTransfer";
			
		}
		
	}
	
	@GetMapping(value = { "/customer/account/enquiry", "/", "/mocha", "/welcome" })
	public String showCustomerEnquiryPage(Model model) {
		CustomerSavingVO customerSavingVO = new CustomerSavingVO();
		// model is map which is used to carry object from controller to view
		model.addAttribute("customerSavingVO", customerSavingVO);
		return "customer/customerEnquiry"; // customerEnquiry.html
	}

	// http://localhost:444/customer/account/registration?cuid=1585a34b5277-dab2-475a-b7b4-042e032e8121603186515
	@GetMapping("/customer/account/registration")
	public String showCustomerRegistrationPage(@RequestParam String cuid, Model model) {

		logger.debug("cuid = " + cuid);
		Optional<CustomerSavingVO> optional = customerEnquiryService.findCustomerEnquiryByUuid(cuid);
		CustomerVO customerVO = new CustomerVO();

		if (!optional.isPresent()) {
			return "customer/error";
		} else {
			// model is used to carry data from controller to the view =- JSP/
			CustomerSavingVO customerSavingVO = optional.get();
			customerVO.setEmail(customerSavingVO.getEmail());
			customerVO.setName(customerSavingVO.getName());
			customerVO.setMobile(customerSavingVO.getMobile());
			customerVO.setAddress(customerSavingVO.getLocation());
			customerVO.setToken(cuid);
			logger.debug(customerSavingVO.toString());
			// model - is hash map which is used to carry data from controller to thyme
			// leaf!!!!!
			// model is similar to request scope in jsp and servlet
			model.addAttribute("customerVO", customerVO);
			return "customer/customerRegistration"; // thyme leaf
		}
	}

	@GetMapping("/customer/account/addPayee")
	public String addPayee(HttpSession session, Model model) {
		logger.debug("addPayee");
		LoginVO loginVO2 = (LoginVO) session.getAttribute("userSessionVO");
		if (loginVO2 == null) {
			model.addAttribute("error", "please Login first");
			return "customer/login";
		} else {
			String response = customerService.findAccountByEmail(loginVO2.getUsername());
			if (response == null) {
				List<PayeeInfoVO> payees = payeeInfoService.getAllPayee(loginVO2.getUsername());
				model.addAttribute("payees", payees);
				return "customer/payeeTable";
			} else {
				model.addAttribute("msg", response);
				return "/customer/dashboard";
			}
		}
	}

	@GetMapping("/customer/addPayee")
	public String loadPayee(HttpSession session, Model model) {
		logger.debug("addPayee");
		LoginVO loginVO2 = (LoginVO) session.getAttribute("userSessionVO");
		if (loginVO2 == null) {
			model.addAttribute("error", "please Login first");
			return "customer/login";
		} else {
			return "customer/addPayee";
		}
	}

	@GetMapping("/customer/account/myAccount")
	public String editProfile(HttpSession session, Model model) {
		logger.debug("editprofile");
		LoginVO loginVO2 = (LoginVO) session.getAttribute("userSessionVO");
		if (loginVO2 == null) {
			model.addAttribute("error", "please Login first");
			return "customer/login";
		} else {
			CustomerVO customerVO = customerService.findProfileByEmail(loginVO2.getUsername());
			model.addAttribute("customerVO", customerVO);// its right here
			List<SecurityQuestionsVO> qstAnswerVO = securityQuestionService.findAll();
			model.addAttribute("questionVOs", qstAnswerVO);
			logger.debug(customerVO.toString());

		}
		return "customer/customerProfileEdit";
	}

	@GetMapping("/customer/account/myAccount/customerAccountHome")
	public String CustomerHome(HttpSession session, Model model) {
		logger.debug("editprofile");
		LoginVO loginVO2 = (LoginVO) session.getAttribute("userSessionVO");
		if (loginVO2 == null) {
			model.addAttribute("error", "please Login first");
			return "customer/login";
		} else {
			CustomerVO customerVO = customerService.findProfileByEmail(loginVO2.getUsername());
			model.addAttribute("customerVO", customerVO);
			logger.debug(customerVO.toString());
		}
		return "customer/customerAccountHome";
	}

	@GetMapping("/login")
	public String CustomerLogin() {
		return "customer/login";
	}

	@GetMapping("/customer/forgotPassword")
	public String forgotPass(Model model) {
		return "customer/forgotPassword";
	}

	@PostMapping("/customer/showQuestion")
	public String resetPassword(@RequestParam String email, Model model) {
		Optional<LoginVO> loginVO = loginService.findUserByUsername(email);
		if (loginVO.isPresent()) {
			CustomerSecurityQueAnsVO customerSecurityQueAnsVO = securityQuestionService.getByEmail(email);
			model.addAttribute("que", customerSecurityQueAnsVO);
			return "customer/validateSecurityQ";
		} else {
			model.addAttribute("error", "Not valid email address");
			return "customer/forgotPassword";
		}
	}

	@PostMapping("/customer/changePassword")
	public String saveCustomerQuestions(@ModelAttribute ChangePasswordVO changePasswordVO, Model model,
			HttpSession session) {
		LoginVO loginVO2 = (LoginVO) session.getAttribute("userSessionVO");
		String loginid = loginVO2.getUsername();
		changePasswordVO.setLoginid(loginid);
		String viewName = "customer/dashboard";
		boolean status = loginService.checkPasswordValid(loginid, changePasswordVO.getCurrentPassword());
		if (status) {
			if (changePasswordVO.getNewPassword().equals(changePasswordVO.getConfirmPassword())) {
				viewName = "customer/dashboard";
				loginService.changePassword(changePasswordVO);
			} else {
				model.addAttribute("error", "Sorry , your new password and confirm passwords are not same!");
				return "customer/login"; // login.html
			}
		} else {
			model.addAttribute("error", "Sorry , your username and password are not valid!");
			return "customer/login"; // login.html
		}
		return viewName;
	}

	@PostMapping("/customer/securityQuestion")
	public String saveCustomerQuestions(
			@ModelAttribute("customerSecurityQueAnsVO") CustomerSecurityQueAnsVO customerSecurityQueAnsVO, Model model,
			HttpSession session) {
		LoginVO loginVO2 = (LoginVO) session.getAttribute("userSessionVO");
		String loginid = loginVO2.getUsername();
		customerSecurityQueAnsVO.setLoginid(loginid);
		securityQuestionService.save(customerSecurityQueAnsVO);
		//
		return "customer/chagePassword";
	}

	@PostMapping("/customer/account/registration")
	public String createCustomer(@ModelAttribute CustomerVO customerVO, Model model) {
		// saving customer into database
		logger.debug(customerVO.toString());
		customerVO = customerService.createAccount(customerVO);
		// Write code to send email

		EmailVO mail = new EmailVO(customerVO.getEmail(), "javahunk2020@gmail.com",
				"Regarding Customer " + customerVO.getName() + "  userid and password", "", customerVO.getName());
		mail.setUsername(customerVO.getUserid());
		mail.setPassword(customerVO.getPassword());
		emailService.sendUsernamePasswordEmail(mail);
		System.out.println(customerVO);
		model.addAttribute("loginVO", new LoginVO());
		model.addAttribute("message", "Your account has been setup successfully , please check your email.");
		return "customer/login";
	}

	@PostMapping("/customer/account/enquiry")
	public String submitEnquiryData(@ModelAttribute CustomerSavingVO customerSavingVO, Model model) {
		boolean status = customerEnquiryService.emailNotExist(customerSavingVO.getEmail());
		logger.info("Executing submitEnquiryData");
		if (status) {
			CustomerSavingVO response = customerEnquiryService.save(customerSavingVO);
			logger.debug("Hey Customer , your enquiry form has been submitted successfully!!! and appref "
					+ response.getAppref());
			model.addAttribute("message",
					"Hey Customer , your enquiry form has been submitted successfully!!! and appref "
							+ response.getAppref());
		} else {
			model.addAttribute("message", "Sorry , this email is already in use " + customerSavingVO.getEmail());
		}
		return "customer/success"; // customerEnquiry.html

	}

	@PostMapping("/customer/account/update")
	public String updateEditProfile(@ModelAttribute CustomerVO customerVO, Model model, HttpSession session) {
		logger.debug("updateEditProfile");

		LoginVO loginVO = (LoginVO) session.getAttribute("userSessionVO");
		if (loginVO != null) {
			// Boolean check=false;
			// check
			customerService.updateProfile(customerVO);
			List<SecurityQuestionsVO> qstAnswerVO = securityQuestionService.findAll();
			model.addAttribute("questionVOs", qstAnswerVO);
			return "customer/customerProfileEdit";
		} else {
			return "customer/login";
		}
	}

	@PostMapping("/customer/account/savePayee")
	public String savePayee(@ModelAttribute PayeeInfoVO payeeInfoVO, Model model, HttpSession session) throws Exception {
		logger.debug("savePayee");
		LoginVO loginVO = (LoginVO) session.getAttribute("userSessionVO");
		if (loginVO != null) {
			payeeInfoVO.setCustomerId(loginVO.getUsername());
			String response = payeeInfoService.savePayee(payeeInfoVO);
				List<PayeeInfoVO> payees = payeeInfoService.getAllPayee(loginVO.getUsername());
				model.addAttribute("payees", payees);
				model.addAttribute("message",response);
				return "customer/addPayee";
			} else {
			model.addAttribute("message", " Your session has ended, please login again");
			return "customer/login";
		}

	}

	@PostMapping("/customer/validateAnswer")
	public String validateAnswer(@ModelAttribute CustomerSecurityQueAnsVO queAnsVO, Model model) {
		Optional<LoginVO> loginVO = loginService.findUserByUsername(queAnsVO.getLoginid());
		if (loginVO.isPresent()) {
			boolean result = false;
			result = securityQuestionService.validateAns(queAnsVO);
			if (result) {
				model.addAttribute("id", loginVO.get().getUsername());
				model.addAttribute("que", queAnsVO);
				return "customer/newPassword";
			} else {
				model.addAttribute("error", "Not valid answer ");
				model.addAttribute("que", queAnsVO);
				return "customer/validateSecurityQ";
			}
		} else {
			model.addAttribute("error", "Email is not present");
			return "customer/forgotPassword";
		}
	}

	@PostMapping("/customer/newPassword")
	public String newPassword(@ModelAttribute CustomerSecurityQueAnsVO queAnsVO, @RequestParam String newPassword,
			@RequestParam String confirmPassword, Model model) {
		Optional<LoginVO> loginVO = loginService.findUserByUsername(queAnsVO.getLoginid());
		if (loginVO.isPresent()) {
			if (newPassword.length() == 0 && confirmPassword.length() == 0) {
				model.addAttribute("que", queAnsVO);
				model.addAttribute("error", "Password cannot be null");
				return "customer/newPassword";
			}

			else if (newPassword.equals(confirmPassword)) {
				ChangePasswordVO changePasswordVO = new ChangePasswordVO();
				changePasswordVO.setLoginid(loginVO.get().getUsername());
				changePasswordVO.setNewPassword(newPassword);
				loginService.changePassword(changePasswordVO);
				model.addAttribute("message", "Your password has been successfully reset");
				return "customer/login";
			} else {
				model.addAttribute("que", queAnsVO);
				model.addAttribute("error", "Both password show be identicdal");
				return "customer/newPassword";
			}

		}
		model.addAttribute("que", queAnsVO);
		model.addAttribute("error", "your session has ended , Plase restart from Login");
		return "customer/newPassword";

	}

}
