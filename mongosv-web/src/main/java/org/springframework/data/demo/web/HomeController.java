package org.springframework.data.demo.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.demo.domain.Author;
import org.springframework.data.demo.domain.Book;
import org.springframework.data.demo.domain.SearchCriteria;
import org.springframework.data.demo.repository.BookShelf;
import org.springframework.data.demo.repository.DbHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.support.SessionStatus;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	
	private final List<String> categories = 
			Arrays.asList("Java", "Spring", "NoSQL", "Big Data", "MongoDB", "Cloud Foundry", "Scala", "Ruby", "Grails");
	
	private final List<String> years = 
			Arrays.asList("", "2000", "2001", "2002", "2003", "2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011" ,"2012");
	
	@Autowired
	ConversionService cs;
	
	@Autowired
	BookShelf bookShelf;
	
	@Autowired
	DbHelper dbHelper;
	

	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Model model) {
		
		model.addAttribute("version", "The MongoTemplate Version for MongoSV December 4, 2012");
		
		model.addAttribute("bookList", bookShelf.findAll() );
		
		return "home";
	}
	
	/**
	 * Add new book form.
	 */
	@RequestMapping(value = "/new", method = RequestMethod.GET)
	public String newBook(Model model) {
		
		Book book = new Book();
		model.addAttribute("book", book);
		
		model.addAttribute("authors", dbHelper.getAuthors());
		model.addAttribute("categories", categories);
		
		return "addBook";
	}

	/**
	 * Save the new book.
	 */
	@RequestMapping(value = "/new", method = RequestMethod.POST)
	public String addBook(@ModelAttribute("book") Book newBook, BindingResult result, SessionStatus status, HttpServletRequest request) {
		
		if (request.getParameter("_cancel") != null) {
			return "redirect:/";
		}
		if (newBook != null && (newBook.getIsbn() == null || newBook.getIsbn().length() <= 0)) {
			ObjectError error = new ObjectError("book.isbn", "ISBN can't be empty");
			result.addError(error);
		}
		if (result.hasErrors()) {
			return	"addBook";
		}
		if (newBook != null) {
			status.setComplete();
			bookShelf.add(newBook);
		}
		
		return "redirect:/";
	}

	/**
	 * Edit a book.
	 */
	@RequestMapping(value = "/edit/{isbn}", method = RequestMethod.GET)
	public String editBook(@PathVariable("isbn") String isbn, Model model) {
		
		Book book = bookShelf.find(isbn);
		model.addAttribute("book", book );

		model.addAttribute("authors", dbHelper.getAuthors());
		model.addAttribute("categories", categories);

		return "editBook";
	}

	/**
	 * Save an edited book.
	 */
	@RequestMapping(value = "/edit/{isbn}", method = RequestMethod.POST)
	public String modifyBook(@PathVariable("isbn") String isbn, @ModelAttribute("book") Book book, BindingResult result, SessionStatus status, HttpServletRequest request) {
		
		if (request.getParameter("_cancel") != null) {
			return "redirect:/";
		}
		if (request.getParameter("_delete") != null) {
			bookShelf.remove(isbn);
			return "redirect:/";
		}
		if (result.hasErrors()) {
			return "editBook";
		}
		if (book != null) {
			status.setComplete();
			bookShelf.save(book);
		}
		
		return "redirect:/";
	}

	/**
	 * Define search criteria.
	 */
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public String searchCriteria(Model model) {

		SearchCriteria searchCriteria = new SearchCriteria();
		model.addAttribute("search", searchCriteria);

		model.addAttribute("years", years);		
		model.addAttribute("categories", categories);

		List<Book> bookList = new ArrayList<Book>();
		model.addAttribute("bookList", bookList);

		return "search";
	}

	/**
	 * Search for books.
	 */
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	public String searchForBooks(@ModelAttribute("search") SearchCriteria searchCriteria, @ModelAttribute("bookList") ArrayList<Book> bookList, Model model, BindingResult result, SessionStatus status, HttpServletRequest request) {
		
		if (request.getParameter("_cancel") != null) {
			return "redirect:/";
		}
		if (result.hasErrors()) {
			return	"search";
		}
		
		if (searchCriteria != null) {
			status.setComplete();
			bookList.clear();
			bookList.addAll(bookShelf.findByCategoriesOrYear(searchCriteria.getCategories(), searchCriteria.getStartYear()));
		}

		model.addAttribute("years", years);		
		model.addAttribute("categories", categories);

		model.addAttribute("bookList", bookList);
		
		return "search";
	}

	/**
	 * Utility methods
	 */
	@RequestMapping(value={"/dump"}, method=RequestMethod.GET)
	public String dump(Model model) {
		model.addAttribute("bookdata", dbHelper.getDump(Book.class));
		model.addAttribute("authordata", dbHelper.getDump(Author.class));
		return "dump";
	}
}
