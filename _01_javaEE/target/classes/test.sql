
create table ResultTable (" +
              "`user` STRING, " +
              "windowEndTime TIMESTAMP(3), " +
              "cnt BIGINT)" +
              " WITH (" +
              "'connector' = 'print')
