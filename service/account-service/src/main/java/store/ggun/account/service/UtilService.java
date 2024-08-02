package store.ggun.account.service;


public interface UtilService {
    int createRandomInteger (int start, int end);
    double createRandomDouble (double start, double end);

    String createRandomName();

    String createRandomTitle();

    String createRandomContent();

    String createRandomCompany();
    String createRandomJob();

    String createRandomUsername();
    String createAccountNumber(String acType);
}
