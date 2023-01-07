package pl.marcinm312.springquestionsanswers.shared.filter;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.io.Writer;

@Slf4j
@Setter
public class Paginator extends SimpleTagSupport {

	private String uri;
	private int currPage;
	private int totalPages;
	private int maxLinks = 10;

	private Writer getWriter() {
		return getJspContext().getOut();
	}

	@Override
	public void doTag() throws JspException {
		Writer out = getWriter();

		boolean lastPage = currPage == totalPages;
		int pgStart = Math.max(currPage - maxLinks / 2, 1);
		int pgEnd = pgStart + maxLinks;
		if (pgEnd > totalPages + 1) {
			int diff = pgEnd - totalPages;
			pgStart -= diff - 1;
			if (pgStart < 1) {
				pgStart = 1;
			}
			pgEnd = totalPages + 1;
		}

		try {
			out.write("<ul class=\"pagination\">");

			if (currPage > 1) {
				out.write(constructLink(1, "&lt;&lt;", "page-item page-pre"));
				out.write(constructLink(currPage - 1, "&lt;", "page-item"));
			}

			for (int i = pgStart; i < pgEnd; i++) {
				if (i == currPage) {
					out.write("<li class=\"page-item active" + (lastPage && i == totalPages ? " page-item page-next" : "") +
							"\">" + "<a class=\"page-link\" href=\"javascript:void(0)\">" + currPage + "</a>" + "</li>");
				}
				else {
					out.write(constructLink(i));
				}
			}

			if (!lastPage) {
				out.write(constructLink(currPage + 1, "&gt;", "page-item"));
				out.write(constructLink(totalPages, "&gt;&gt;", "page-item page-next"));
			}

			out.write("</ul>");

		} catch (IOException ex) {
			String errorMessage = String.format("Error in Paginator tag: %s", ex.getMessage());
			log.error(errorMessage, ex);
			throw new JspException(errorMessage, ex);
		}
	}

	private String constructLink(int page) {
		return constructLink(page, String.valueOf(page), null);
	}

	private String constructLink(int page, String text, String className) {
		StringBuilder link = new StringBuilder("<li");
		if (className != null) {
			link.append(" class=\"");
			link.append(className);
			link.append("\"");
		}
		link.append(">")
				.append("<a class=\"page-link\" href=\"")
				.append(uri.replace("xxx", String.valueOf(page)))
				.append("\">")
				.append(text)
				.append("</a></li>");
		return link.toString();
	}
}
