package com.rab3tech.customer.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rab3tech.customer.dao.repository.CustomerQuestionsAnsRepository;
import com.rab3tech.customer.dao.repository.LoginRepository;
import com.rab3tech.customer.dao.repository.SecurityQuestionsRepository;
import com.rab3tech.dao.entity.CustomerQuestionAnswer;
import com.rab3tech.dao.entity.Login;
import com.rab3tech.dao.entity.SecurityQuestions;
import com.rab3tech.vo.CustomerSecurityQueAnsVO;
import com.rab3tech.vo.SecurityQuestionsVO;

@Transactional
@Service
public class SecurityQuestionServiceImpl implements SecurityQuestionService {

	@Autowired
	private SecurityQuestionsRepository questionsRepository;
	
	@Autowired
	private LoginRepository loginRepository;
	
	@Autowired
	private CustomerQuestionsAnsRepository customerQuestionsAnsRepository;
	
	@Override
	public void save(CustomerSecurityQueAnsVO customerSecurityQueAnsVO) {
		
		//Deleting all customer questions for existing user
		List<CustomerQuestionAnswer> customerQuestionAnswers=customerQuestionsAnsRepository.findQuestionAnswer(customerSecurityQueAnsVO.getLoginid());
		if(customerQuestionAnswers.size()>0)
		customerQuestionsAnsRepository.deleteAll(customerQuestionAnswers);
		
		CustomerQuestionAnswer customerQuestionAnswer1=new CustomerQuestionAnswer();
		Login login=loginRepository.findById(customerSecurityQueAnsVO.getLoginid()).get();
		
		String quetionText=questionsRepository.findById(Integer.parseInt(customerSecurityQueAnsVO.getSecurityQuestion1())).get().getQuestions();
		//String quetionText= customerSecurityQueAnsVO.getSecurityQuestion1();
		customerQuestionAnswer1.setQuestion(quetionText);
		customerQuestionAnswer1.setAnswer(customerSecurityQueAnsVO.getSecurityQuestionAnswer1());
		customerQuestionAnswer1.setDoe(new Timestamp(new Date().getTime()));
		customerQuestionAnswer1.setDom(new Timestamp(new Date().getTime()));
		customerQuestionAnswer1.setLogin(login);
		customerQuestionsAnsRepository.save(customerQuestionAnswer1);
		
		CustomerQuestionAnswer customerQuestionAnswer2=new CustomerQuestionAnswer();
		quetionText=questionsRepository.findById(Integer.parseInt(customerSecurityQueAnsVO.getSecurityQuestion2())).get().getQuestions();
		//quetionText= customerSecurityQueAnsVO.getSecurityQuestion2();
		customerQuestionAnswer2.setQuestion(quetionText);
		customerQuestionAnswer2.setAnswer(customerSecurityQueAnsVO.getSecurityQuestionAnswer2());
		customerQuestionAnswer2.setDoe(new Timestamp(new Date().getTime()));
		customerQuestionAnswer2.setDom(new Timestamp(new Date().getTime()));
		customerQuestionAnswer2.setLogin(login);
		customerQuestionsAnsRepository.save(customerQuestionAnswer2);
	
	}
	
	
	@Override
	public List<SecurityQuestionsVO>  findAll(){
		List<SecurityQuestions>  securityQuestions=questionsRepository.findAll();
		List<SecurityQuestionsVO> questionsVOs=new ArrayList<>();
		for(SecurityQuestions questions:securityQuestions) {
			SecurityQuestionsVO questionsVO=new SecurityQuestionsVO();
			BeanUtils.copyProperties(questions, questionsVO);
			questionsVOs.add(questionsVO);
		}
		return questionsVOs;
		/*return securityQuestions.stream().map(tt->{
			SecurityQuestionsVO questionsVO=new SecurityQuestionsVO();
			BeanUtils.copyProperties(tt, questionsVO);
			return questionsVO;
		}).collect(Collectors.toList());*/
	}


	@Override
	public CustomerSecurityQueAnsVO getByEmail(String email) {
		CustomerSecurityQueAnsVO queAnsVO = new CustomerSecurityQueAnsVO();
		List<CustomerQuestionAnswer> customerSecurityQueAnsVO = customerQuestionsAnsRepository.findQuestionAnswer(email);
		queAnsVO.setSecurityQuestion1(customerSecurityQueAnsVO.get(0).getQuestion());
		queAnsVO.setSecurityQuestion2(customerSecurityQueAnsVO.get(1).getQuestion());
		queAnsVO.setLoginid(email);
		return queAnsVO;
	}


	@Override
	public boolean validateAns(CustomerSecurityQueAnsVO queAnsVO) {
		List<CustomerQuestionAnswer> customerSecurityQueAnsVO = customerQuestionsAnsRepository.findQuestionAnswer(queAnsVO.getLoginid());
		boolean validate=false;
		if(customerSecurityQueAnsVO.get(0).getQuestion().equalsIgnoreCase(queAnsVO.getSecurityQuestion1())&&
				customerSecurityQueAnsVO.get(1).getQuestion().equalsIgnoreCase(queAnsVO.getSecurityQuestion2())) {
			validate=true;
		}
		if(customerSecurityQueAnsVO.get(0).getAnswer().equalsIgnoreCase(queAnsVO.getSecurityQuestionAnswer1()) &&
				customerSecurityQueAnsVO.get(1).getAnswer().equalsIgnoreCase(queAnsVO.getSecurityQuestionAnswer2()) ) {
			validate=true;
		}		
		else {
			validate=false;
		}
		return validate;
	}
	
}
