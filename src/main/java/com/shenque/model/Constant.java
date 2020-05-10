package com.shenque.model;
/**
 * xiao.py
 */
public class Constant {
    public enum TaobaoRange {
        MORE_THAN_500( "500以上"),
        RANGE_500_300( "500-300"),
        RANGE_300_200( "300-200"),
        RANGE_200_100( "200-100"),
        RANGE_100_1( "100-1");


        private String standardMessage;

        TaobaoRange( String standardMessage) {
            this.standardMessage = standardMessage;
        }


        public String getStandardMessage() {
            return standardMessage;
        }

        public void setStandardMessage(String standardMessage) {
            this.standardMessage = standardMessage;
        }
    }


    public enum SortRange {
        YOU_HUI_JUAN_BI_SORT(1),
        SALE_PRIVE_SORT(2),
        PRIVE_SORT(3);

        private Integer sortId;

        SortRange( Integer sortId) {
            this.sortId = sortId;
        }


        public Integer getSortId() {
            return sortId;
        }

        public void setSortId(Integer sortId) {
            this.sortId = sortId;
        }
    }


}
