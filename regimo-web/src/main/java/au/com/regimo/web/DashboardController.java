package au.com.regimo.web;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

import au.com.regimo.core.domain.Dashboard;
import au.com.regimo.core.form.DataTablesColumnDef;
import au.com.regimo.core.form.DataTablesSearchCriteria;
import au.com.regimo.core.form.DataTablesSearchResult;
import au.com.regimo.core.service.DashboardService;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

	private DashboardService service;

	private final static DataTablesSearchCriteria datatable = new DataTablesSearchCriteria(
			"viewName,columnCount,columnWidth", "standardUpdate", Lists.newArrayList(
			new DataTablesColumnDef("dashboard.viewName","55%"),
			new DataTablesColumnDef("dashboard.columnCount","5%"),
			new DataTablesColumnDef("dashboard.columnWidth","25%"),
			new DataTablesColumnDef("label.action","15%")));

	@RequestMapping(method=RequestMethod.GET)
	@ModelAttribute("datatable")
	public DataTablesSearchCriteria browse() {
		return datatable;
	}

	@RequestMapping(method=RequestMethod.POST)
	@ResponseBody
	public DataTablesSearchResult<?> browse(
			@ModelAttribute DataTablesSearchCriteria searchCriteria){
		return service.searchFullText(searchCriteria);
	}

	@RequestMapping(value = "/view", method = RequestMethod.GET)
	public void view(ModelMap modelMap, @RequestParam Long id) {
		service.loadModel(modelMap, id);
	}

	@RequestMapping(value = "/new", method = RequestMethod.GET)
	public void create(ModelMap modelMap) {
		service.loadModel(modelMap);
	}

	@RequestMapping(value = "/new", method = RequestMethod.POST)
	public String create(ModelMap modelMap,
			@Valid Dashboard entity, BindingResult result) {
		return service.saveModel(modelMap, entity, result) ?
				"redirect:edit?id="+entity.getId() : null;
	}

	@RequestMapping(value = "/edit", method = RequestMethod.GET)
	public void update(ModelMap modelMap, @RequestParam Long id) {
		service.loadModel(modelMap, id);
	}

	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	public void update(ModelMap modelMap,
			@Valid Dashboard entity, BindingResult result) {
		service.saveModel(modelMap, entity, result);
	}

	@Inject
	public void setService(DashboardService service) {
		this.service = service;
	}

}
