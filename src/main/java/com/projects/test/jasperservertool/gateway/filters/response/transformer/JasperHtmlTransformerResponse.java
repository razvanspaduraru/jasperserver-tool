package com.projects.test.jasperservertool.gateway.filters.response.transformer;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

@Slf4j
public class JasperHtmlTransformerResponse implements TransformResponse {
    @Override
    public String applyTransform(String originalBody) {
        Document doc = Jsoup.parse(originalBody);

        String[] selectors = {
                "#banner",
                "#frameFooter",
                ".buttonSet #close",
                ".buttonSet #fileOptions",
                ".buttonSet #schedule"
        };

        for (String selector : selectors) {
            Elements elements = doc.select(selector);
            for (Element el : elements) {
                el.remove();
            }
        }

        Element frame = doc.getElementById("frame");
        if (frame != null) {
            frame.attr("style", "top: 0; bottom: 0;");
        }

        return doc.outerHtml();
    }
}
