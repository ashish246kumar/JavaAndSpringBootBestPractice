public class EmploymentTypeValidation implements ValidationStrategy{

	private static final Logger log = LoggerFactory.getLogger(EmploymentTypeValidation.class);
	@Override
	public void validate(PolicyRequest policyRequest,Map<String,String> responseMap,PolicyCondition policyCondition) {
		log.info("EmploymentTypeValidation::validate for productId: {}, employmentType: {}",policyRequest.getProductId(),policyRequest.getEmploymentType());
		
  		 boolean isValid =validateEmploymentType(policyRequest,policyCondition);
  		 System.out.println("isValid"+isValid);
  		 if(isValid) {
  			 System.out.println(policyCondition.getReasonCode());
  			responseMap.put(policyCondition.getReasonCode(), policyCondition.getMessage());
  		 }
  		 log.info("EmploymentTypeValidation::validate Execution Ended");
	}

	
	 public boolean validateEmploymentType(PolicyRequest policyRequest,PolicyCondition policyCondition) {
			
			List<String> productIdList=policyCondition.getProductIdList();
			ExpressionParser parser = new SpelExpressionParser(); 
			EvaluationContext context = new StandardEvaluationContext();
			context.setVariable("employmentType",policyRequest.getEmploymentType());
			return productIdList.contains(policyRequest.getProductId())
		    		&&!parser.parseExpression(policyCondition.getExpression()).getValue(context,Boolean.class);
		}
	
}
***********************************************************************************
public class LoanAmountValidation implements ValidationStrategy{

	private static final Logger log = LoggerFactory.getLogger(LoanAmountValidation.class);
	@Override
	public void validate(PolicyRequest policyRequest,Map<String,String> responseMap,PolicyCondition policyCondition) {
		 log.info("LoanAmountValidation::validate for productId: {}, requestedLoanAmount: {}", policyRequest.getProductId(), policyRequest.getRequestedLoanAmount());
		
		 boolean isNotValidLoanAmount =validateLoanAmount(policyRequest,policyCondition);
	
		 if(isNotValidLoanAmount) {
			
			 responseMap.put(policyCondition.getReasonCode(), policyCondition.getMessage());
		 }
		
		 log.info("LoanAmountValidation::validate Execution Ended");
	}
      public boolean validateLoanAmount(PolicyRequest policyRequest,PolicyCondition policyCondition) {
		
		List<String> productIdList=policyCondition.getProductIdList();
		ExpressionParser parser = new SpelExpressionParser(); 
		EvaluationContext context = new StandardEvaluationContext();
		context.setVariable("productValue",policyRequest.getProductValue());
		context.setVariable("requestedLoanAmount",policyRequest.getRequestedLoanAmount());
		return productIdList.contains(policyRequest.getProductId())
	    		&&parser.parseExpression(policyCondition.getExpression()).getValue(context,Boolean.class);
	}
  *************************************************************************************************************************************************************
    public class NationValidation implements ValidationStrategy{

	private static final Logger log = LoggerFactory.getLogger(NationValidation.class);

	
	@Override
	public void validate(PolicyRequest policyRequest,Map<String,String> responseMap,PolicyCondition policyCondition) {
		log.info("NationValidation::validate Execution Started");
		boolean isValid=validateNationality(policyRequest,policyCondition);
	    if(isValid) {
			 
			responseMap.put(policyCondition.getReasonCode(), policyCondition.getMessage());
         }
	    log.info("NationValidation::validate Execution ended");
	}
	public boolean validateNationality(PolicyRequest policyRequest,PolicyCondition policyCondition) {
		
		List<String> productIdList=policyCondition.getProductIdList();
		ExpressionParser parser = new SpelExpressionParser(); 
		EvaluationContext context = new StandardEvaluationContext();
		context.setVariable("nationality",policyRequest.getCustomerNationality());
		return productIdList.contains(policyRequest.getProductId())
	    		&&parser.parseExpression(policyCondition.getExpression()).getValue(context,Boolean.class);
	}

}
******************************************************************************************************************
  public interface ValidationStrategy {

	void validate(PolicyRequest policyRequest,Map<String,String> responseMap,PolicyCondition policyCondition);
}

****************************************************************
  public class StrategyFactory {

	private static final Map<String,ValidationStrategy> strategies=new HashMap<>();
	static {
		strategies.put("nationality", new NationValidation());
		strategies.put("tenure",new TenureValidation());
		strategies.put("loanAmount",new LoanAmountValidation());
		strategies.put("employmentType",new EmploymentTypeValidation());
	}
	public static ValidationStrategy getValidationStrategy(String type) {
		return strategies.get(type);
	}
}
************************************************************************************************************************************
  @Service
public class PolicyValidatorServiceImpl implements PolicyValidatorService{

	@Autowired
	public PolicyEngineRequestLogRepository policyEngineRequestLogRepository;
	private static final Logger log = LoggerFactory.getLogger(PolicyValidatorServiceImpl.class);
	@Autowired
	PolicyConditionRepository  policyConditionRepository;
	
	private static final String PREFIX = "JIO";
    private static final int ID_LENGTH =3;
    
	public PolicyResponse validatePolicy(PolicyRequest policyRequest) {
		log.info("PolicyValidatorServiceImpl::validatePolicy execution Sarted for productId {}",policyRequest.getProductId());
		Map<String,String> responseMap=new HashMap<>();
		ValidationStrategy validationStrategy=null;
		List<PolicyCondition> policyConditionList=policyConditionRepository.findAll();
		
		for(PolicyCondition policyCondition:policyConditionList) {
			
			validationStrategy=StrategyFactory.getValidationStrategy(policyCondition.getName());
			 validationStrategy.validate(policyRequest,responseMap,policyCondition);
			 
			}
		
		  String leadId=generateID();

		PolicyResponse policyResponse=PolicyResponse.builder()
				.productId(policyRequest.getProductId()).leadId(leadId).response(responseMap).build();
		log.info("PolicyValidatorServiceImpl::validatePolicy execution Ended");
		return policyResponse;
	}
	************************************************************************************************************************************************************
  
