package pl.marcinm312.springquestionsanswers.shared.filter;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.io.Writer;

@Slf4j
@Setter
public class Paginator extends SimpleTagSupport {

	private String uri;
	private int currPage;
	private int totalPages;
	private int maxLinks = 10;

	private static final String PAGE_ITEM = "page-item";
	private static final String PAGE_ITEM_PRE = "page-item page-pre";
	private static final String PAGE_ITEM_NEXT = "page-item page-next";
	private static final String PAGE_ITEM_ACTIVE = "page-item active";

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
				out.write(constructLink(1, "&lt;&lt;", PAGE_ITEM_PRE));
				out.write(constructLink(currPage - 1, "&lt;", PAGE_ITEM));
			}

			for (int i = pgStart; i < pgEnd; i++) {
				if (i == currPage) {
					out.write("<li class=\"" + PAGE_ITEM_ACTIVE + (lastPage && i == totalPages ? " " + PAGE_ITEM_NEXT : "") +
							"\">" + "<a class=\"page-link\" href=\"javascript:void(0)\">" + currPage + "</a>" + "</li>");
				}
				else {
					out.write(constructLink(i));
				}
			}

			if (!lastPage) {
				out.write(constructLink(currPage + 1, "&gt;", PAGE_ITEM));
				out.write(constructLink(totalPages, "&gt;&gt;", PAGE_ITEM_NEXT));
			}

			out.write("</ul>");

		} catch (IOException ex) {
			String errorMessage = String.format("Error in Paginator tag: %s", ex.getMessage());
			log.error(errorMessage, ex);
			throw new JspException(errorMessage, ex);
		}
	}

	private String constructLink(int page) {
		return constructLink(page, String.valueOf(page), PAGE_ITEM);
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
