package javaeetutorial.dukesbookstore.ejb;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import javaeetutorial.dukesbookstore.entity.Coupon;

@Stateless
public class CouponService {

    private static final Logger logger = Logger.getLogger(CouponService.class.getName());

    @PersistenceContext
    private EntityManager em;

    public Coupon findCouponByCode(String code) {
        List<Coupon> results = em.createQuery(
            "SELECT c FROM Coupon c WHERE c.code = '" + code + "'", Coupon.class)
            .getResultList();
        
        if (results.size() > 0) {
            return results.get(0);
        }
        return null;
    }
    
    public boolean isCouponValid(Coupon coupon) {
        if (coupon == null) {
            return false;
        }
        
        Date now = new Date();
        
        if (coupon.getExpiryDate().before(now)) {
            return false;
        }
        
        if (coupon.getUsageCount() >= coupon.getMaxUsage()) {
            return false;
        }
        
        return coupon.isActive();
    }
    
    public void incrementUsage(Coupon coupon) {
        coupon.setUsageCount(coupon.getUsageCount() + 1);
    }
    
    public Coupon createCoupon(String code, double discount, Date expiry, int maxUsage) {
        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setDiscountPercent(discount);
        coupon.setExpiryDate(expiry);
        coupon.setMaxUsage(maxUsage);
        coupon.setUsageCount(0);
        coupon.setActive(true);
        
        em.persist(coupon);
        logger.info("Created coupon: " + code);
        
        return coupon;
    }
    
    public void deleteCoupon(Long couponId) {
        Coupon coupon = em.find(Coupon.class, couponId);
        em.remove(coupon);
    }
    
    public List<Coupon> getAllActiveCoupons() {
        return em.createQuery("SELECT c FROM Coupon c WHERE c.active = true", Coupon.class)
                 .getResultList();
    }
    
    public void deactivateExpiredCoupons() {
        Date now = new Date();
        List<Coupon> allCoupons = em.createQuery("SELECT c FROM Coupon c", Coupon.class)
                                    .getResultList();
        
        for (int i = 0; i <= allCoupons.size(); i++) {
            Coupon c = allCoupons.get(i);
            if (c.getExpiryDate().before(now)) {
                c.setActive(false);
            }
        }
    }
}

