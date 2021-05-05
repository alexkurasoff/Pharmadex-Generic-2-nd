package org.msh.pharmadex.domain;
import java.util.Date;

import javax.persistence.*;

import org.msh.pharmadex.domain.enums.PayType;
/**
 * Embedded payment description
 */
@Embeddable
public class FeePayment {
	
	@Column(name="`payment_form`", length=10)
	@Enumerated(EnumType.STRING)
	private PayType payment_form;
	
	private Long feeSum;
	
	@Column(name="`payment_receipt`", length=32)	
	private String payment_receipt;
					
	@Temporal(TemporalType.DATE)
	private Date payment_payDate;
		
	private boolean payment_received = false;
	
	
	public Long getFeeSum() {
		return feeSum;
	}

	public void setFeeSum(Long feeSum) {
		this.feeSum = feeSum;
	}
	public String getPayment_receipt() {
		return payment_receipt;
	}

	public void setPayment_receipt(String payment_receipt) {
		this.payment_receipt = payment_receipt;
	}
	public Date getPayment_payDate() {
		return payment_payDate;
	}

	public void setPayment_payDate(Date payment_payDate) {
		this.payment_payDate = payment_payDate;
	}
	
	public boolean isPayment_received() {
		return payment_received;
	}

	public void setPayment_received(boolean payment_received) {
		this.payment_received = payment_received;
	}

	public PayType getPayment_form() {
		return payment_form;
	}

	public void setPayment_form(PayType payment_form) {
		this.payment_form = payment_form;
	}
	
}
