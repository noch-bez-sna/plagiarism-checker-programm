public class FinanceCalculator {

    // Расчет сложных процентов
    public static double compoundInterest(double principal, double rate, int years) {
        return principal * Math.pow(1 + rate / 100, years);
    }

    // Ежемесячный платеж по кредиту
    public static double monthlyPayment(double loanAmount, double annualRate, int months) {
        double monthlyRate = annualRate / 12 / 100;
        return loanAmount * monthlyRate * Math.pow(1 + monthlyRate, months) /
                (Math.pow(1 + monthlyRate, months) - 1);
    }

    // Будущая стоимость аннуитета
    public static double futureValueAnnuity(double payment, double rate, int periods) {
        return payment * (Math.pow(1 + rate / 100, periods) - 1) / (rate / 100);
    }

    // Расчет инфляции
    public static double inflationAdjustment(double amount, double inflationRate, int years) {
        return amount / Math.pow(1 + inflationRate / 100, years);
    }

    // Расчет НДС
    public static double calculateVAT(double amount, double vatRate) {
        return amount * vatRate / 100;
    }

    // Чистая приведенная стоимость (NPV)
    public static double calculateNPV(double[] cashFlows, double discountRate) {
        double npv = 0;
        for (int i = 0; i < cashFlows.length; i++) {
            npv += cashFlows[i] / Math.pow(1 + discountRate / 100, i);
        }
        return npv;
    }
}