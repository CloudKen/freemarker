package freemarker.template;

import java.io.Serializable;
import java.util.Date;

import freemarker.template.utility.StringUtil;

/**
 * Represents a version number plus the further qualifiers and build into. This is
 * mostly used for representing a FreeMarker version number, but must also be able
 * to parse the version strings of 3rd party libraries.
 * 
 * @see Configuration#getVersion()
 * 
 * @since 2.3.20
 */
public final class Version implements Serializable {
    
    private final int major;
    private final int minor;
    private final int micro;
    private final String extraInfo;
    private final String originalStringValue;
    
    private final Boolean gaeCompliant;
    private final Date buildDate;
    
    private final int intValue;
    private String calculatedStringValue;  // not final because it's calculated on demand
    private int hashCode;  // not final because it's calculated on demand

    public Version(String stringValue) {
        this(stringValue, null, null);
    }
    
    public Version(String stringValue, Boolean gaeCompliant, Date buildDate) {
        stringValue = stringValue.trim();
        originalStringValue = stringValue; 
        
        int[] parts = new int[3];
        String extraInfoTmp = null;
        {
            int partIdx = 0;
            boolean valid = false;
            for (int i = 0; i < stringValue.length(); i++) {
                char c = stringValue.charAt(i);
                if (c >= '0' && c <= '9') {
                    parts[partIdx] = parts[partIdx] * 10 + (c - '0');
                    valid = true;
                } else {
                    if (c == '.') {
                        if (partIdx == 2) {
                            extraInfoTmp = stringValue.substring(i);
                            break;
                        } else {
                            partIdx++;
                        }
                    } else {
                        extraInfoTmp = stringValue.substring(i);
                        break;
                    }
                }
            }
            if (!valid) throw new IllegalArgumentException(
                    "A version number string " + StringUtil.jQuote(stringValue)
                    + " must start with a number.");
            
            if (extraInfoTmp != null) {
                char firstChar = extraInfoTmp.charAt(0); 
                if (firstChar == '.' || firstChar == '-' || firstChar == '_') {
                    extraInfoTmp = extraInfoTmp.substring(1);
                }
            }
        }
        extraInfo = extraInfoTmp;
        
        major = parts[0];
        minor = parts[1];
        micro = parts[2];
        intValue = calculateIntValue();
        
        this.gaeCompliant = gaeCompliant;
        this.buildDate = buildDate;
        
    }

    public Version(int major, int minor, int micro) {
        this(major, minor, micro, null, null, null);
    }
    
    public Version(int major, int minor, int micro, String extraInfo, Boolean gaeCompatible, Date buildDate) {
        this.major = major;
        this.minor = minor;
        this.micro = micro;
        this.extraInfo = extraInfo;
        this.gaeCompliant = gaeCompatible;
        this.buildDate = buildDate;
        intValue = calculateIntValue();
        originalStringValue = null;
    }

    private int calculateIntValue() {
        return intValueFor(major, minor, micro);
    }
    
    static public int intValueFor(int major, int minor, int micro) {
        return major * 1000000 + minor * 1000 + micro;
    }
    
    private String getStringValue() {
        if (originalStringValue != null) return originalStringValue;
        // Switch to double-check + volatile with Java 5
        synchronized (this) {
            if (calculatedStringValue == null) {
                calculatedStringValue = major + "." + minor + "." + micro;
                if (extraInfo != null) calculatedStringValue += "-" + extraInfo; 
            }
            return calculatedStringValue;
        }
    }
    
    /**
     * Contains the major.minor.micor numbers and the extraInfo part, not the other information.
     */
    public String toString() {
        return getStringValue();
    }

    /**
     * The 1st version number, like 1 in "1.2.3".
     */
    public int getMajor() {
        return major;
    }

    /**
     * The 2nd version number, like 2 in "1.2.3".
     */
    public int getMinor() {
        return minor;
    }

    /**
     * The 3rd version number, like 3 in "1.2.3".
     */
    public int getMicro() {
        return micro;
    }

    /**
     * The arbitrary string after the micro version number without leading dot, dash or underscore,
     * like "RC03" in "2.4.0-RC03".
     * This is usually a qualifier (RC, SNAPHOST, nightly, beta, etc) and sometimes build info (like
     * date).
     */
    public String getExtraInfo() {
        return extraInfo;
    }
    
    /**
     * @return The Google App Engine compliance, or {@code null}.
     */
    public Boolean isGAECompliant() {
        return gaeCompliant;
    }

    /**
     * @return The build date if known, or {@code null}.
     */
    public Date getBuildDate() {
        return buildDate;
    }

    /**
     * @return major * 1000000 + minor * 1000 + micro.
     */
    public int intValue() {
        return intValue;
    }

    public int hashCode() {
        int r = hashCode;
        if (r != 0) return r;
        synchronized (this) {
            if (hashCode == 0) {
                final int prime = 31;
                int result = 1;
                result = prime * result + (buildDate == null ? 0 : buildDate.hashCode());
                result = prime * result + (extraInfo == null ? 0 : extraInfo.hashCode());
                result = prime * result + (gaeCompliant == null ? 0 : gaeCompliant.hashCode());
                result = prime * result + intValue;
                String stringValue = getStringValue();
                result = prime * result + (stringValue == null ? 0 : stringValue.hashCode());
                if (result == 0) result = -1;  // 0 is reserved for "not set"
                hashCode = result;
            }
            return hashCode;
        }
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        Version other = (Version) obj;

        if (intValue != other.intValue) return false;
        
        if (other.hashCode() != hashCode()) return false;
        
        if (buildDate == null) {
            if (other.buildDate != null) return false;
        } else if (!buildDate.equals(other.buildDate)) {
            return false;
        }
        
        if (extraInfo == null) {
            if (other.extraInfo != null) return false;
        } else if (!extraInfo.equals(other.extraInfo)) {
            return false;
        }
        
        if (gaeCompliant == null) {
            if (other.gaeCompliant != null) return false;
        } else if (!gaeCompliant.equals(other.gaeCompliant)) {
            return false;
        }
        
        return true;
    }
    
}
