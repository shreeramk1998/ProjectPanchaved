package com.panchaved.web;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpServerErrorException;

import com.panchaved.enitity.Doctor;
import com.panchaved.enitity.Patient;
import com.panchaved.enitity.PatientBill;
import com.panchaved.service.BillService;
import com.panchaved.service.DoctorService;
import com.panchaved.service.PatientService;
import com.panchaved.util.AppSession;
import com.panchaved.util.CaseTaking;
import com.panchaved.util.Prescriptor;

@Controller
@RequestMapping("/doctor")
public class DoctorController{

	@Autowired
	PatientService	pService;
	@Autowired
	DoctorService dService;
	@Autowired
	BillService bService;
	
	
	@RequestMapping(method = RequestMethod.GET)
	public String docDashboard(Model model) {
		model.addAttribute("patient",new Patient());
		
		return "doctorDashboard.jsp";
	}
	
	@RequestMapping(value="/patient/review", method = RequestMethod.GET)
	public @ResponseBody Patient reviewPatient(Model model,HttpServletRequest req ,@RequestParam("patientId") Integer id)
	{
			Patient patient = pService.getSelectedPatient(id);
			HttpSession session = req.getSession(false);
			session.setAttribute("PID", id);
			System.out.println("inside review");
			model.addAttribute("patient",patient);
		return patient;
	}
	
	
	@RequestMapping(value = "/patient/casetaking",method = RequestMethod.GET)
	public @ResponseBody ArrayList<Object> loadCase(@RequestParam("patId") Integer patId)
	{	
		ArrayList<Object> casetakings= new ArrayList<Object>();
		Patient patient = pService.getSelectedPatient(patId);
		System.out.println("INSIDE LOADCASE"+patient.toString());
		try
		{
			casetakings = pService.getCaseTaking(patient);
		}catch (Exception e) {
			e.printStackTrace();
		}
	return casetakings;
	}
	
	
	
	@RequestMapping(value="/patient/casetaking", method = RequestMethod.POST)
	public  String updatePatient(Model model,@ModelAttribute("patient") Patient patient) {
		model.addAttribute("casetake",new CaseTaking());
		model.addAttribute("patID",patient.getPatientId());
		if(pService.updatePatient(patient)) {
//			model.addAttribute("success_msg","Patient updated with Id: "+patient.getPatientId());
		}else {
			model.addAttribute("success_msg","Sorry couldnt Update patient! Please Retry");
		}
		return "caseTaking.jsp";
	}
	
	@RequestMapping(value="/update",method = RequestMethod.GET)
	public String updateDoctor(Model model, @RequestParam("doctorId")Long id)
	{
		model.addAttribute("doc",new Doctor());
		model.addAttribute("doctor",dService.getSelectedDoctor(id));
		return "manageMe.jsp";
	}
	
	@RequestMapping(value="/update",method = RequestMethod.POST)
	public String updateDoctor(Model model,@ModelAttribute("doc") Doctor doc) {

		if(dService.updateDoc(doc)) {
			model.addAttribute("success_msg","Doctor updated with Id: "+doc.getDoctorID());
		}else {
			model.addAttribute("success_msg","Sorry couldnt Update doctor! Please Retry");
		}
		return "manageMe.jsp";
	}
	

	@RequestMapping(value="/casetakingSummary",method = RequestMethod.POST)
	public String takeCase(Model model,@ModelAttribute("casetake") CaseTaking casetake )
	{
		System.out.println("POST casetaking");
		Patient p = pService.getSelectedPatient(casetake.getPatientID());
		model.addAttribute("pat",p);
		model.addAttribute("diagnosis",new Prescriptor());
        
		if(casetake.getOe().equals(""))
		{
			
		}
		else
			//Create .txt to write casetaking
			pService.saveCasetaking(p,casetake);
			
		return "caseSummary(OPD).jsp";
	}
	
	
	@RequestMapping(value="/casetakingSummary",method = RequestMethod.GET)
	public @ResponseBody ArrayList<Object> loadPresc(@RequestParam("patId") Integer patId)
	{	
		ArrayList<Object> prescriptions= new ArrayList<Object>();
		Patient patient = pService.getSelectedPatient(patId);
		System.out.println("INSIDE LOADPRESC"+patient.toString());
		try
		{
			prescriptions = pService.getPrescriptions(patient);
		}catch (Exception e) {
			e.printStackTrace();
		}
	return prescriptions;
	}
	
	@RequestMapping(value = "/bill" , method = RequestMethod.GET)
	public String showBill( Model model, HttpServletRequest req ) throws IOException {
		HttpSession session = req.getSession(false);
		model.addAttribute("pat",pService.getSelectedPatient((int)session.getAttribute("PID")));
		Map<String, String> map = bService.readCPS();
		model.addAttribute("CPSMap", map);
		model.addAttribute("patientBill", new PatientBill());
		System.out.println("ajsdyfgusydfguyg");
		return "billReceipt.jsp";
	}
	
	@RequestMapping(value = "/bill" , method = RequestMethod.POST)
	public String saveBill( Model model, @ModelAttribute("diagnosis")Prescriptor prescriptor,@ModelAttribute("pat")Patient p) throws IOException {
		Map<String, String> map = bService.readCPS();
		model.addAttribute("CPSMap", map);
		Patient patient = pService.getSelectedPatient(p.getPatientId());
		pService.savePrescription(patient, prescriptor);
		model.addAttribute("pat",p);
		return "billReceipt.jsp";
	}
	
	@RequestMapping(value = "/setCost" , method = RequestMethod.GET)
	public String showCPS( HttpServletRequest req,Model model, HttpSession session ) throws IOException {
		model.addAttribute("cpsStatus","");
		return "costPerSitting.jsp";
	}
	
	
	@RequestMapping(value = "/setCost" , method = RequestMethod.POST) //
	public String saveCPS(Model model, @RequestBody Map<String,String> myrequestMap) throws IOException { //
		System.out.println(myrequestMap);
		bService.saveCps(myrequestMap);
		model.addAttribute("cpsStatus","CHANGES SAVED!!!!");
		return "costPerSitting.jsp"; 
	}
	
	@RequestMapping(value = "/getCPSMap")
	public @ResponseBody Map<String, String> getCPSMap(Model model) throws IOException {
		System.out.println("showing map");
		return bService.readCPS();
	}
		
}	
