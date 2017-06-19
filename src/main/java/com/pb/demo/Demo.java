/**
 * 
 */
package com.pb.demo;

import javax.xml.bind.annotation.XmlRootElement;

import com.pb.jaxb.utility.JAXBUtility;

/**
 * @author Prashant Basawa
 *
 */
public class Demo {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Employee employee = new Employee();
		employee.setName("Prashant' Basawa");
		employee.setAge(40);
		employee.setDepartment("IT>Dept");
		employee.setId("12345");
		employee.setSalary(10000);
		
		System.out.println(JAXBUtility.marshalObjectWithoutValidation(employee));
	}
	
	@XmlRootElement
	public static class Employee {
	    private String name;
	    private String id;
	    private String department;
	    private int age;
	    private int salary;
	    
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getDepartment() {
			return department;
		}
		public void setDepartment(String department) {
			this.department = department;
		}
		public int getAge() {
			return age;
		}
		public void setAge(int age) {
			this.age = age;
		}
		public int getSalary() {
			return salary;
		}
		public void setSalary(int salary) {
			this.salary = salary;
		}    
	}
}
