package com.secureskytech.multipartcsrfgen;

import java.nio.charset.Charset;

import javax.servlet.http.Part;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IndexController {

    @Autowired
    ApplicationContext applicationContext;

    @RequestMapping("/")
    public String index(Model m) {
        String[] charsets = new String[] { "UTF-8", "Shift_JIS", "windows-31j", "EUC-JP", "ISO-2022-JP", "ISO-8859-1" };
        m.addAttribute("charsets", charsets);
        return "index";
    }

    @PostMapping("/upload-multipart-request")
    public String uploadMultipartRequest(@RequestParam String url, @RequestParam String csname,
            @RequestParam Part httpRequest, Model m) throws Exception {
        final Charset charset = Charset.forName(csname);

        m.addAttribute("httpRequest", httpRequest);
        byte[] httpRequestAsBytes = StreamUtils.copyToByteArray(httpRequest.getInputStream());
        final HttpMultipartRequest mpreq = HttpMultipartRequest.parse(httpRequestAsBytes, charset);
        m.addAttribute("mpreq", mpreq);
        return "upload-multipart-request";
    }
}
