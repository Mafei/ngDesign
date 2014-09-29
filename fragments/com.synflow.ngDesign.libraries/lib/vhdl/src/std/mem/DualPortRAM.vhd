-------------------------------------------------------------------------------
-- Copyright (c) 2012-2013 Synflow SAS.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--    Nicolas Siret - initial API and implementation and/or initial documentation
--    Matthieu Wipliez - refactoring and maintenance
-------------------------------------------------------------------------------

-------------------------------------------------------------------------------
-- Title      : Dual-port inferred RAM
-- Author     : Nicolas Siret (nicolas.siret@synflow.com)
--              Matthieu Wipliez (matthieu.wipliez@synflow.com)
-- Standard   : VHDL'93
-------------------------------------------------------------------------------

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
-------------------------------------------------------------------------------



-------------------------------------------------------------------------------
-- Entity
-------------------------------------------------------------------------------
entity DualPortRAM is
  generic (
    depth : integer := 8;
    width : integer := 16;
    numAdditionalRegisters : integer := 0);
  port (
    clock_a, clock_b : in std_logic;
    --
    address_a   : in std_logic_vector(depth - 1 downto 0);
    data_a      : in std_logic_vector(width - 1 downto 0);
    data_a_send : in std_logic;
    --
    address_b   : in std_logic_vector(depth - 1 downto 0);
    data_b      : in std_logic_vector(width - 1 downto 0);
    data_b_send : in std_logic;
    --
    q_a : out std_logic_vector(width - 1 downto 0);
    q_b : out std_logic_vector(width - 1 downto 0));
end DualPortRAM;
-------------------------------------------------------------------------------



-------------------------------------------------------------------------------
-- Architecture
-------------------------------------------------------------------------------
architecture rtl_DualPortRAM of DualPortRAM is

  -----------------------------------------------------------------------------
  -- RAM contents
  -----------------------------------------------------------------------------
  type ram_type is array (0 to 2**depth - 1) of std_logic_vector(width - 1 downto 0);
  shared variable ram : ram_type;

-------------------------------------------------------------------------------
begin

  access_a: process(clock_a)
  begin
    if rising_edge(clock_a) then
      if data_a_send = '1' then
        ram(to_integer(unsigned(address_a))) := data_a;
        q_a <= data_a;
      else
        q_a <= ram(to_integer(unsigned(address_a)));
      end if;
    end if;
  end process access_a;

  access_b: process(clock_b)
  begin
    if rising_edge(clock_b) then
      if data_b_send = '1' then
        ram(to_integer(unsigned(address_b))) := data_b;
        q_b <= data_b;
      else
        q_b <= ram(to_integer(unsigned(address_b)));
      end if;
    end if;
  end process access_b;

end rtl_DualPortRAM;
