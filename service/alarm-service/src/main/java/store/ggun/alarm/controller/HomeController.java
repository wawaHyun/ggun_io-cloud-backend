package store.ggun.alarm.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequiredArgsConstructor
public class HomeController {

    public String Date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());

    @GetMapping("/api/chats/test")
    public String hello() {
        return Date + "Welcome To admin service";
    }
}
