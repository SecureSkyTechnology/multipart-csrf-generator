package com.secureskytech.multipartcsrfgen;

import java.nio.charset.Charset;

import javax.servlet.http.Part;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.secureskytech.multipartcsrfgen.CsrfHistory.CsrfItem;

import lombok.Data;

@Controller
@SessionAttributes("csrfHistory")
public class IndexController {

    @Autowired
    ApplicationContext applicationContext;

    @ModelAttribute("csrfHistory")
    public CsrfHistory createCsrfHistory() {
        return new CsrfHistory();
    }

    @RequestMapping("/")
    public String index(Model m) {
        String[] charsets = new String[] { "UTF-8", "Shift_JIS", "windows-31j", "EUC-JP", "ISO-2022-JP", "ISO-8859-1" };
        m.addAttribute("charsets", charsets);
        return "index";
    }

    @RequestMapping("/dummy")
    public String dummy(Model m) {
        return index(m);
    }

    @Data
    public static class UploadRequestForm {
        @NotEmpty
        public String url;
        @NotEmpty
        public String csname;
        public boolean enableAutoAccess;
    }

    @PostMapping("/upload-multipart-request")
    public String uploadMultipartRequest(UploadRequestForm uploadRequestForm, @RequestParam Part httpRequest, Model m,
            CsrfHistory csrfHistory) throws Exception {
        final Charset charset = Charset.forName(uploadRequestForm.csname);

        m.addAttribute("httpRequest", httpRequest); // TODO アップロードされない場合は、nullにはならない。size = -1 になるのかな？
        System.out.println("===============>>>>>>>>>>>> uploaded Part.size(); = [[[[" + httpRequest.getSize() + "]]]"); // TODO
        byte[] httpRequestAsBytes = StreamUtils.copyToByteArray(httpRequest.getInputStream());
        final HttpMultipartRequest mpreq = HttpMultipartRequest.parse(httpRequestAsBytes, charset);
        CsrfItem csrfItem = new CsrfItem(uploadRequestForm.url, charset, mpreq, "<s>\"TODO&'</s>"); //TODO
        csrfHistory.items.add(0, csrfItem);
        m.addAttribute("csrfItem", csrfItem);
        return "csrf-form";
    }

    @RequestMapping("/csrf-form/{token}")
    public String csrfForm(@PathVariable String token, Model m, CsrfHistory csrfHistory) {
        for (CsrfItem i : csrfHistory.items) {
            if (i.token.equals(token)) {
                m.addAttribute("csrfItem", i);
                break;
            }
        }
        return "csrf-form";
    }

    @RequestMapping("/csrf-form-attack/{token}")
    public String csrfFormAttack(@PathVariable String token, Model m, CsrfHistory csrfHistory) {
        for (CsrfItem i : csrfHistory.items) {
            if (i.token.equals(token)) {
                m.addAttribute("csrfItem", i);
                break;
            }
        }
        return "csrf-form";
    }
}
