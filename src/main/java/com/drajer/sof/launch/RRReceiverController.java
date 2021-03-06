package com.drajer.sof.launch;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RRReceiverController {

  private final Logger logger = LoggerFactory.getLogger(RRReceiverController.class);

  @CrossOrigin
  @RequestMapping(value = "/api/rrReceiver", method = RequestMethod.POST)
  public ResponseEntity<String> rrReceiver(
      @RequestBody String obj, HttpServletRequest request, HttpServletResponse response) {
    try {
      logger.info("Received Obj:::::" + obj);
    } catch (Exception e) {
      logger.error("Error in Processing Eicr XML");
    }

    return new ResponseEntity<String>("Success", HttpStatus.OK);
  }
}
