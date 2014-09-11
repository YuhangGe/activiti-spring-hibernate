package me.xiaoge.web;

import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/login")
public class AuthenticationController {
	
	@RequestMapping(method = RequestMethod.GET)
	public void login() {
		
	}

    @RequestMapping(method = RequestMethod.POST)
    public void doLogin() {

    }
}
