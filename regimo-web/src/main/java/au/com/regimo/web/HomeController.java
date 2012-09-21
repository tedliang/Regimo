package au.com.regimo.web;

import java.security.Principal;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import au.com.regimo.core.domain.Dashboard;
import au.com.regimo.core.domain.User;
import au.com.regimo.core.domain.UserDashlet;
import au.com.regimo.core.repository.DashboardRepository;
import au.com.regimo.core.repository.UserDashletRepository;
import au.com.regimo.core.service.SecurityService;
import au.com.regimo.core.service.UserService;
import au.com.regimo.core.utils.SecurityUtils;
import au.com.regimo.core.utils.TextGenerator;
import au.com.regimo.server.wordpress.domain.WpTerm;
import au.com.regimo.server.wordpress.repository.WpPostRepository;
import au.com.regimo.server.wordpress.repository.WpTermRepository;
import au.com.regimo.web.form.UserEntryForm;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

	private UserService userService;
	private DashboardRepository dashboardRepository;
	private UserDashletRepository userDashletRepository;
	private WpTermRepository wpTermRepository;
	private WpPostRepository wpPostRepository;
	private SecurityService securityService;

	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(ModelMap map, Principal user) {
		if(user!=null && !SecurityUtils.isUserInRole("ADMIN")){
			return "redirect:/profile";
		}
		return home(map);
	}

	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String home(ModelMap map) {
		Dashboard content = dashboardRepository.findByViewName("HomeContent");
		map.addAttribute("content", content);
		return "home";
	}

	@RequestMapping(value="/signin", method=RequestMethod.GET)
	public void signin() {
	}

	@RequestMapping(value="/profile", method=RequestMethod.GET)
	public void viewProfile(ModelMap map) {
		map.addAttribute("user",  SecurityUtils.getCurrentUser());
	}

	@RequestMapping(value="/profile/edit", method=RequestMethod.GET)
	public void editProfile(ModelMap map) {
		map.addAttribute("user", SecurityUtils.getCurrentUser());
	}

	@RequestMapping(value = "/profile/edit", method = RequestMethod.POST)
	public String updateUser(@Valid UserEntryForm form,  ModelMap map) {
		User user = userService.findOne(SecurityUtils.getCurrentUserId());
		userService.save(form.getUpdatedUser(user));
		SecurityUtils.updateCurrentUser(user);
		return "redirect:/profile";
	}

	@RequestMapping(value = "/contents")
	public void dashboard(ModelMap map) {
		Dashboard dashboard = dashboardRepository.findByViewName("dashboard");
		map.addAttribute("dashboard", dashboard);
	}
	
	@RequestMapping(value = "/contents/{userDashletId}")
	@ResponseBody
	public String browse(@PathVariable Long userDashletId, ModelMap map) {
		UserDashlet userDashlet = userDashletRepository.findOne(userDashletId);
		String dashModel = userDashlet.getDashlet().getModel();
		if(dashModel!=null){
			if("wpTerms".equals(dashModel)){
				map.addAttribute("wpTerms", wpTermRepository.findByTaxonomyCategory());
			}
			else if("wpPosts".equals(dashModel)){
				map.addAttribute("wpPosts", wpPostRepository.findBySlug(
						userDashlet.getDashlet().getParameter()));
			}
			else if("wpPost".equals(dashModel)){
				map.addAttribute("wpPost", wpPostRepository.findByPostName(
						userDashlet.getDashlet().getParameter()));
			}
			else if("wpMenu".equals(dashModel)){
				List<WpTerm> terms = wpTermRepository.findByTaxonomyCategory();
				for(WpTerm term : terms){
					term.setWpPosts(wpPostRepository.findBySlug(term.getSlug()));
				}
				map.addAttribute("wpMenu", terms);
			}
			else{
				return "TODO: Freemarkered content "+ userDashlet.getDashlet().getContent();
			}
		}
		map.addAttribute("user", SecurityUtils.getCurrentUser());
		map.addAttribute("security", securityService);
		return TextGenerator.generateText(String.format(
				"[#macro acl attribute][#if security.isAuthorized(attribute)][#nested][/#if][/#macro]%s%s",
				"[#macro url attribute][#local link=security.getAuthorizedUrl(attribute)][#if link!=''][#nested link][/#if][/#macro]",
				userDashlet.getDashlet().getContent()), map);
	}
	
	@Inject
	public void setDashboardRepository(DashboardRepository dashboardRepository) {
		this.dashboardRepository = dashboardRepository;
	}

	@Inject
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@Inject
	public void setUserDashletRepository(UserDashletRepository userDashletRepository) {
		this.userDashletRepository = userDashletRepository;
	}

	@Inject
	public void setWpTermRepository(WpTermRepository wpTermRepository) {
		this.wpTermRepository = wpTermRepository;
	}

	@Inject
	public void setWpPostRepository(WpPostRepository wpPostRepository) {
		this.wpPostRepository = wpPostRepository;
	}

	@Inject
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

}
